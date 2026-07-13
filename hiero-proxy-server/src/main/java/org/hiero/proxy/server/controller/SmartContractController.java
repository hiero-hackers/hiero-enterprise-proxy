package org.hiero.proxy.server.controller;

import com.hedera.hashgraph.sdk.ContractId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hiero.base.SmartContractClient;
import org.hiero.base.data.ContractCallResult;
import org.hiero.base.mirrornode.ContractRepository;
import org.hiero.proxy.server.dto.request.ContractCallRequest;
import org.hiero.proxy.server.dto.request.ContractCreateRequest;
import org.hiero.proxy.server.dto.response.ContractCallResultResponse;
import org.hiero.proxy.server.dto.response.ContractResponse;
import org.hiero.proxy.server.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/contracts")
@Tag(
        name = "Smart Contracts",
        description = "Operations for deploying and interacting with smart contracts on the Hiero network, "
                + "as well as querying contract information from the mirror node.")
public class SmartContractController {

    private final SmartContractClient smartContractClient;
    private final ContractRepository contractRepository;

    public SmartContractController(SmartContractClient smartContractClient, ContractRepository contractRepository) {
        this.smartContractClient = Objects.requireNonNull(smartContractClient, "smartContractClient must not be null");
        this.contractRepository = Objects.requireNonNull(contractRepository, "contractRepository must not be null");
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(
            summary = "Create a smart contract",
            description = "Deploys a new smart contract to the network using the provided Base64-encoded bytecode. "
                    + "The operator account pays the transaction fee.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Contract deployed successfully.",
                    content = @Content(
                            schema = @Schema(implementation = ContractResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "contractId": "0.0.9999",
                                      "deleted": false,
                                      "createdTimestampEpochSecond": 1700000000,
                                      "expirationTimestampEpochSecond": 1800000000,
                                      "evmAddress": "0x000000000000000000000000000000000000270f",
                                      "memo": ""
                                    }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bytecode is missing, blank, or not valid Base64.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Contract could not be deployed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ContractResponse> createContract(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Base64-encoded contract bytecode.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ContractCreateRequest.class),
                            examples = @ExampleObject(value = """
                                    { "bytecode": "NjA4MDYwNDA1MjM0ODAxNTEw..." }""")))
            @RequestBody ContractCreateRequest request) throws Exception {

        if (request.bytecode() == null || request.bytecode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bytecode must not be blank");
        }

        // The hiero-enterprise-java library expects the hex string itself as UTF-8 bytes
        // (exactly as if reading a .bin file and calling getBytes(UTF_8)), NOT decoded binary.
        String hex = request.bytecode().trim();
        if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        }
        if (!hex.matches("[0-9a-fA-F]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Bytecode must be a valid hex string containing only characters 0-9 and a-f");
        }
        final byte[] bytes = hex.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        ContractId contractId;
        try {
            contractId = smartContractClient.createContract(bytes);
        } catch (Exception e) {
            // Dig into the exception chain to find the real network error
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to deploy contract: " + e.getMessage() + " | Root cause: " + root.getMessage(), e);
        }
        
        // Return a mock/partial response since we just created it and might not have full info instantly from mirror node
        // In a real scenario, we'd fetch the info from the network or mirror node
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ContractResponse(contractId.toString(), false, null, null, null, null)
        );
    }

    // ─── Call Function ────────────────────────────────────────────────────────

    @PostMapping("/{contractId}/call")
    @Operation(
            summary = "Call a smart contract function",
            description = "Executes a function on an existing smart contract. "
                    + "Currently supports no-argument function calls.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Contract function executed successfully.",
                    content = @Content(
                            schema = @Schema(implementation = ContractCallResultResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "gasUsed": 25000,
                                      "costInTinybars": 150000
                                    }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid function name.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Contract call failed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ContractCallResultResponse> callContractFunction(
            @Parameter(description = "The Hedera contract ID.", required = true, example = "0.0.9999")
            @PathVariable("contractId") String contractId,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The name of the function to call.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ContractCallRequest.class),
                            examples = @ExampleObject(value = """
                                    { "functionName": "myFunction" }""")))
            @RequestBody ContractCallRequest request) throws Exception {

        if (request.functionName() == null || request.functionName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Function name must not be blank");
        }

        ContractCallResult result = smartContractClient.callContractFunction(contractId, request.functionName());
        return ResponseEntity.ok(ContractCallResultResponse.from(result));
    }

    // ─── Get All Contracts ────────────────────────────────────────────────────

    @GetMapping
    @Operation(
            summary = "List all smart contracts",
            description = "Fetches a paginated list of all smart contracts from the mirror node.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Contracts retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = ContractResponse.class, type = "array"))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve contracts.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ContractResponse>> getAllContracts() throws Exception {
        return ResponseEntity.ok(
                contractRepository.findAll()
                        .getData()
                        .stream()
                        .map(ContractResponse::from)
                        .collect(Collectors.toList())
        );
    }

    // ─── Get Contract by ID ───────────────────────────────────────────────────

    @GetMapping("/{contractId}")
    @Operation(
            summary = "Get a smart contract by ID",
            description = "Fetches information about a specific smart contract from the mirror node.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Contract retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = ContractResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Contract not found.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve contract.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ContractResponse> getContractById(
            @Parameter(description = "The Hedera contract ID.", required = true, example = "0.0.9999")
            @PathVariable("contractId") String contractId) throws Exception {

        return contractRepository.findById(contractId)
                .map(ContractResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Contract not found: " + contractId));
    }
}
