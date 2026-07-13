package org.hiero.proxy.server.controller;

import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.HookEntityId;
import com.hedera.hashgraph.sdk.HookId;
import com.hedera.hashgraph.sdk.PrivateKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hiero.base.HookClient;
import org.hiero.proxy.server.dto.request.HookStoreRequest;
import org.hiero.proxy.server.dto.response.HookStoreResponse;
import org.hiero.proxy.server.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/hooks")
@Tag(
        name = "Hooks",
        description = "Operations for managing transaction hooks on the Hiero network.")
public class HookController {

    private final HookClient hookClient;

    public HookController(HookClient hookClient) {
        this.hookClient = Objects.requireNonNull(hookClient, "hookClient must not be null");
    }

    @PostMapping
    @Operation(
            summary = "Store or update a hook",
            description = "Stores or updates a hook on the Hiero network using the provided hook ID and signer keys. "
                    + "The operator account pays the transaction fee.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Hook stored successfully.",
                    content = @Content(
                            schema = @Schema(implementation = HookStoreResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "hookId": "0.0.12345",
                                      "message": "Hook 0.0.12345 stored successfully."
                                    }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format or invalid private keys.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Hook could not be stored on the network.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<HookStoreResponse> storeHook(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The hook ID and required signer keys.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = HookStoreRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "hookId": "0.0.12345",
                                      "signerKeys": ["302e020100300506032b657004220420..."]
                                    }""")))
            @RequestBody HookStoreRequest request) throws Exception {

        if (request.contractId() == null || request.contractId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contract ID must not be blank");
        }
        if (request.hookNumber() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hook number must not be null");
        }

        if (request.signerKeys() == null || request.signerKeys().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one signer key must be provided");
        }

        final HookId hookId;
        try {
            ContractId cid = ContractId.fromString(request.contractId());
            HookEntityId entityId = new HookEntityId(cid);
            hookId = new HookId(entityId, request.hookNumber());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid contract ID or hook number");
        }

        final List<PrivateKey> keys;
        try {
            keys = request.signerKeys().stream()
                    .map(PrivateKey::fromString)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more signer keys are invalid");
        }

        // Pass an empty list for storage updates for now since we don't have a DTO representation for EvmHookStorageUpdate
        hookClient.storeHook(hookId, Collections.emptyList(), keys);

        return ResponseEntity.ok(HookStoreResponse.of(request.contractId() + ":" + request.hookNumber()));
    }
}
