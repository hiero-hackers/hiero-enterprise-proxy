package org.hiero.proxy.server.controller;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TokenId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hiero.base.NftClient;
import org.hiero.base.mirrornode.NftRepository;
import org.hiero.proxy.server.dto.request.BurnNftsRequest;
import org.hiero.proxy.server.dto.request.CreateNftTypeRequest;
import org.hiero.proxy.server.dto.request.MintNftRequest;
import org.hiero.proxy.server.dto.request.MintNftsRequest;
import org.hiero.proxy.server.dto.request.NftBatchTransferRequest;
import org.hiero.proxy.server.dto.request.NftTransferRequest;
import org.hiero.proxy.server.dto.request.TokenAssociateRequest;
import org.hiero.proxy.server.dto.request.TokenBatchAssociateRequest;
import org.hiero.proxy.server.dto.response.NftMintedBatchResponse;
import org.hiero.proxy.server.dto.response.NftMintedResponse;
import org.hiero.proxy.server.dto.response.NftResponse;
import org.hiero.proxy.server.dto.response.NftTypeCreatedResponse;
import org.hiero.proxy.server.dto.response.SuccessResponse;
import org.hiero.proxy.server.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/nfts")
@Tag(name = "NFTs", description = "Operations for managing Non-Fungible Tokens (HTS NFTs) on a Hiero network")
public class NftController {

    private final NftClient nftClient;
    private final NftRepository nftRepository;

    public NftController(NftClient nftClient, NftRepository nftRepository) {
        this.nftClient = Objects.requireNonNull(nftClient, "nftClient must not be null");
        this.nftRepository = Objects.requireNonNull(nftRepository, "nftRepository must not be null");
    }

    // ─── NFT Type Creation ───────────────────────────────────────────────────

    @PostMapping
    @Operation(
            summary = "Create an NFT token type",
            description = "Creates a new NFT token type on the Hiero Token Service (HTS). "
                    + "The operator account is used as both treasury and supply account by default. "
                    + "Provide optional `treasuryAccountId` + `treasuryKey` to use a custom treasury, "
                    + "and/or `supplierKey` to use a custom key for future mint/burn operations.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "NFT type created successfully.",
                    content = @Content(
                            schema = @Schema(implementation = NftTypeCreatedResponse.class),
                            examples = @ExampleObject(value = """
                                    { "tokenId": "0.0.66001" }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request — name or symbol is missing.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "NFT type could not be created on the network.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<NftTypeCreatedResponse> createNftType(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "NFT type definition. Only `name` and `symbol` are required.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateNftTypeRequest.class),
                            examples = @ExampleObject(value = """
                                    { "name": "My Enterprise NFT", "symbol": "MENFT" }""")))
            @RequestBody CreateNftTypeRequest request) throws Exception {

        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NFT type name must not be blank");
        }
        if (request.symbol() == null || request.symbol().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NFT type symbol must not be blank");
        }

        final TokenId tokenId;
        boolean hasTreasury = request.treasuryAccountId() != null && request.treasuryKey() != null;
        boolean hasSupplierKey = request.supplierKey() != null;

        if (hasTreasury && hasSupplierKey) {
            tokenId = nftClient.createNftType(
                    request.name(), request.symbol(),
                    request.treasuryAccountId(), request.treasuryKey(),
                    request.supplierKey());
        } else if (hasTreasury) {
            tokenId = nftClient.createNftType(
                    request.name(), request.symbol(),
                    request.treasuryAccountId(), request.treasuryKey());
        } else if (hasSupplierKey) {
            tokenId = nftClient.createNftType(
                    request.name(), request.symbol(),
                    request.supplierKey());
        } else {
            tokenId = nftClient.createNftType(request.name(), request.symbol());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(NftTypeCreatedResponse.of(tokenId));
    }

    // ─── NFT Instance Queries (mirror node) ─────────────────────────────────

    @GetMapping("/{tokenId}/instances")
    @Operation(
            summary = "List all NFT instances of a type",
            description = "Fetches all active (non-burned) NFT instances for a given token type from the Hiero mirror node. "
                    + "Burned NFTs have a null owner and are automatically excluded from the results.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "NFT instances retrieved successfully.",
                    content = @Content(schema = @Schema(implementation = NftResponse.class, type = "array"))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve NFT instances.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<NftResponse>> listNftsByType(
            @Parameter(description = "The Hedera NFT token type ID.", required = true, example = "0.0.66001")
            @PathVariable("tokenId") String tokenId) throws Exception {
        try {
            return ResponseEntity.ok(
                    nftRepository.findByType(tokenId)
                            .getData()
                            .stream()
                            .map(NftResponse::from)
                            .collect(Collectors.toList())
            );
        } catch (IllegalStateException e) {
            // The mirror node returns account_id=null for burned NFTs; the underlying
            // library fails to parse those entries. Surface a clear message instead of a
            // raw 500 with an opaque JSON parse error.
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not parse NFT data for token " + tokenId
                            + ". The token may contain burned NFTs whose owner is null. "
                            + "Underlying error: " + e.getMessage());
        }
    }

    @GetMapping("/{tokenId}/instances/{serialNumber}")
    @Operation(
            summary = "Get a specific NFT instance",
            description = "Fetches a single NFT instance by token type and serial number from the Hiero mirror node.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "NFT instance retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = NftResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "tokenId": "0.0.66001",
                                      "serialNumber": 1,
                                      "ownerId": "0.0.12345",
                                      "metadata": "aXBmczovL1FtWHh4eHh4eHh4eHh4eHh4eHh4eA=="
                                    }"""))),
            @ApiResponse(
                    responseCode = "404",
                    description = "NFT instance not found.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve NFT instance.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<NftResponse> getNftInstance(
            @Parameter(description = "The Hedera NFT token type ID.", required = true, example = "0.0.66001")
            @PathVariable("tokenId") String tokenId,
            @Parameter(description = "The serial number of the NFT instance.", required = true, example = "1")
            @PathVariable("serialNumber") long serialNumber) throws Exception {
        return nftRepository.findByTypeAndSerial(tokenId, serialNumber)
                .map(NftResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "NFT not found: tokenId=" + tokenId + ", serial=" + serialNumber));
    }

    @GetMapping("/owner/{ownerId}/instances")
    @Operation(
            summary = "List all NFT instances owned by an account",
            description = "Fetches all NFT instances currently owned by the specified account from the Hiero mirror node.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "NFT instances retrieved successfully.",
                    content = @Content(schema = @Schema(implementation = NftResponse.class, type = "array"))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve NFT instances.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<NftResponse>> listNftsByOwner(
            @Parameter(description = "The Hedera account ID of the owner.", required = true, example = "0.0.12345")
            @PathVariable("ownerId") String ownerId) throws Exception {
        return ResponseEntity.ok(
                nftRepository.findByOwner(ownerId)
                        .getData()
                        .stream()
                        .map(NftResponse::from)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{tokenId}/instances/owner/{ownerId}")
    @Operation(
            summary = "List NFT instances of a type owned by an account",
            description = "Fetches all NFT instances of a specific token type that are owned by the given account.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "NFT instances retrieved successfully.",
                    content = @Content(schema = @Schema(implementation = NftResponse.class, type = "array"))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve NFT instances.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<NftResponse>> listNftsByOwnerAndType(
            @Parameter(description = "The Hedera NFT token type ID.", required = true, example = "0.0.66001")
            @PathVariable("tokenId") String tokenId,
            @Parameter(description = "The Hedera account ID of the owner.", required = true, example = "0.0.12345")
            @PathVariable("ownerId") String ownerId) throws Exception {
        return ResponseEntity.ok(
                nftRepository.findByOwnerAndType(ownerId, tokenId)
                        .getData()
                        .stream()
                        .map(NftResponse::from)
                        .collect(Collectors.toList())
        );
    }

    // ─── Associate / Dissociate ───────────────────────────────────────────────

    @PostMapping("/{tokenId}/associate")
    @Operation(
            summary = "Associate account with NFT type",
            description = "Associates a Hedera account with an NFT token type so it can hold and receive NFTs of that type. "
                    + "An account must be associated before it can receive a transfer of this NFT type.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account associated with NFT type successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Account 0.0.12345 associated with NFT type 0.0.66001 successfully." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Association could not be executed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> associateNft(
            @Parameter(description = "The Hedera NFT token type ID.", required = true, example = "0.0.66001")
            @PathVariable("tokenId") String tokenId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Account ID and private key to authorise the association.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TokenAssociateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "accountId": "0.0.12345",
                                      "accountKey": "302e020100300506032b657004220420aabbcc..."
                                    }""")))
            @RequestBody TokenAssociateRequest request) throws Exception {
        nftClient.associateNft(tokenId, request.accountId(), request.accountKey());
        return ResponseEntity.ok(SuccessResponse.of(
                "Account " + request.accountId() + " associated with NFT type " + tokenId + " successfully."));
    }

    @DeleteMapping("/{tokenId}/associate")
    @Operation(
            summary = "Dissociate account from NFT type",
            description = "Removes the association between a Hedera account and an NFT token type. "
                    + "The account's NFT balance for this type must be zero before dissociating.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account dissociated from NFT type successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Account 0.0.12345 dissociated from NFT type 0.0.66001 successfully." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Dissociation could not be executed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> dissociateNft(
            @Parameter(description = "The Hedera NFT token type ID.", required = true, example = "0.0.66001")
            @PathVariable("tokenId") String tokenId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Account ID and private key to authorise the dissociation.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TokenAssociateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "accountId": "0.0.12345",
                                      "accountKey": "302e020100300506032b657004220420aabbcc..."
                                    }""")))
            @RequestBody TokenAssociateRequest request) throws Exception {
        nftClient.dissociateNft(tokenId, request.accountId(), request.accountKey());
        return ResponseEntity.ok(SuccessResponse.of(
                "Account " + request.accountId() + " dissociated from NFT type " + tokenId + " successfully."));
    }

    @PostMapping("/associate")
    @Operation(
            summary = "Batch associate account with multiple NFT types",
            description = "Associates a Hedera account with multiple NFT token types at once.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account associated with NFT types successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Account 0.0.12345 associated with 2 NFT types successfully." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Association could not be executed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> batchAssociateNfts(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of NFT type IDs, Account ID, and private key.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TokenBatchAssociateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "tokenIds": ["0.0.66001", "0.0.66002"],
                                      "accountId": "0.0.12345",
                                      "accountKey": "302e020100300506032b657004220420aabbcc..."
                                    }""")))
            @RequestBody TokenBatchAssociateRequest request) throws Exception {
        List<TokenId> tokenIds = request.tokenIds().stream()
                .map(TokenId::fromString)
                .collect(Collectors.toList());
        nftClient.associateNft(
                tokenIds,
                AccountId.fromString(request.accountId()),
                PrivateKey.fromString(request.accountKey()));
        return ResponseEntity.ok(SuccessResponse.of(
                "Account " + request.accountId() + " associated with " + tokenIds.size() + " NFT types successfully."));
    }

    @DeleteMapping("/associate")
    @Operation(
            summary = "Batch dissociate account from multiple NFT types",
            description = "Removes the association between a Hedera account and multiple NFT token types at once.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account dissociated from NFT types successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Account 0.0.12345 dissociated from 2 NFT types successfully." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Dissociation could not be executed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> batchDissociateNfts(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of NFT type IDs, Account ID, and private key.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TokenBatchAssociateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "tokenIds": ["0.0.66001", "0.0.66002"],
                                      "accountId": "0.0.12345",
                                      "accountKey": "302e020100300506032b657004220420aabbcc..."
                                    }""")))
            @RequestBody TokenBatchAssociateRequest request) throws Exception {
        List<TokenId> tokenIds = request.tokenIds().stream()
                .map(TokenId::fromString)
                .collect(Collectors.toList());
        nftClient.dissociateNft(
                tokenIds,
                AccountId.fromString(request.accountId()),
                PrivateKey.fromString(request.accountKey()));
        return ResponseEntity.ok(SuccessResponse.of(
                "Account " + request.accountId() + " dissociated from " + tokenIds.size() + " NFT types successfully."));
    }

    // ─── Mint ────────────────────────────────────────────────────────────────

    @PostMapping("/{tokenId}/mint")
    @Operation(
            summary = "Mint a single NFT",
            description = "Mints one new NFT instance of the given type. "
                    + "Provide metadata as a Base64-encoded string (e.g. an IPFS URI). "
                    + "Omit `supplyKey` if the operator account is the supply account.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "NFT minted successfully. Returns the serial number of the new instance.",
                    content = @Content(
                            schema = @Schema(implementation = NftMintedResponse.class),
                            examples = @ExampleObject(value = """
                                    { "serialNumber": 1 }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Metadata is missing or blank.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Mint operation failed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<NftMintedResponse> mintNft(
            @Parameter(description = "The Hedera NFT token type ID.", required = true, example = "0.0.66001")
            @PathVariable("tokenId") String tokenId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Base64-encoded metadata and optional supply key.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = MintNftRequest.class),
                            examples = @ExampleObject(value = """
                                    { "metadata": "aXBmczovL1FtWHh4eHh4eHh4eHh4eHh4eHh4eA==" }""")))
            @RequestBody MintNftRequest request) throws Exception {

        if (request.metadata() == null || request.metadata().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NFT metadata must not be blank");
        }
        byte[] metadataBytes = Base64.getDecoder().decode(request.metadata());

        final long serialNumber;
        if (request.supplyKey() != null) {
            serialNumber = nftClient.mintNft(
                    TokenId.fromString(tokenId),
                    PrivateKey.fromString(request.supplyKey()),
                    metadataBytes);
        } else {
            serialNumber = nftClient.mintNft(TokenId.fromString(tokenId), metadataBytes);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(NftMintedResponse.of(serialNumber));
    }

    @PostMapping("/{tokenId}/mint/batch")
    @Operation(
            summary = "Mint multiple NFTs",
            description = "Mints multiple NFT instances of the given type in a single transaction. "
                    + "Each entry in `metadataList` produces one NFT with a unique serial number. "
                    + "Omit `supplyKey` if the operator account is the supply account.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "NFTs minted successfully. Returns the serial numbers in the same order as the metadata list.",
                    content = @Content(
                            schema = @Schema(implementation = NftMintedBatchResponse.class),
                            examples = @ExampleObject(value = """
                                    { "serialNumbers": [1, 2, 3] }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Metadata list is missing or empty.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Mint operation failed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<NftMintedBatchResponse> mintNfts(
            @Parameter(description = "The Hedera NFT token type ID.", required = true, example = "0.0.66001")
            @PathVariable("tokenId") String tokenId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of Base64-encoded metadata strings and optional supply key.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = MintNftsRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "metadataList": [
                                        "aXBmczovL1FtQUFBQUFBQUFBQUFBQUE=",
                                        "aXBmczovL1FtQkJCQkJCQkJCQkJCQkI="
                                      ]
                                    }""")))
            @RequestBody MintNftsRequest request) throws Exception {

        if (request.metadataList() == null || request.metadataList().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metadata list must not be empty");
        }

        byte[][] metadataArray = request.metadataList().stream()
                .map(Base64.getDecoder()::decode)
                .toArray(byte[][]::new);

        final List<Long> serialNumbers;
        if (request.supplyKey() != null) {
            serialNumbers = nftClient.mintNfts(
                    TokenId.fromString(tokenId),
                    PrivateKey.fromString(request.supplyKey()),
                    metadataArray);
        } else {
            serialNumbers = nftClient.mintNfts(TokenId.fromString(tokenId), metadataArray);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(NftMintedBatchResponse.of(serialNumbers));
    }

    // ─── Burn ────────────────────────────────────────────────────────────────

    @PostMapping("/{tokenId}/burn")
    @Operation(
            summary = "Burn NFT instances",
            description = "Permanently destroys one or more NFT instances by serial number. "
                    + "The NFTs must be held by the treasury account. "
                    + "Omit `supplyKey` if the operator account is the supply account.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "NFT(s) burned successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "NFT(s) [1, 2] of type 0.0.66001 burned successfully." }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Serial numbers list is missing or empty.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Burn operation failed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> burnNfts(
            @Parameter(description = "The Hedera NFT token type ID.", required = true, example = "0.0.66001")
            @PathVariable("tokenId") String tokenId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Set of serial numbers to burn and optional supply key.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = BurnNftsRequest.class),
                            examples = @ExampleObject(value = """
                                    { "serialNumbers": [1, 2] }""")))
            @RequestBody BurnNftsRequest request) throws Exception {

        if (request.serialNumbers() == null || request.serialNumbers().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serial numbers must not be empty");
        }

        TokenId tid = TokenId.fromString(tokenId);
        if (request.supplyKey() != null) {
            nftClient.burnNfts(tid, request.serialNumbers(), PrivateKey.fromString(request.supplyKey()));
        } else {
            nftClient.burnNfts(tid, request.serialNumbers());
        }
        return ResponseEntity.ok(SuccessResponse.of(
                "NFT(s) " + request.serialNumbers() + " of type " + tokenId + " burned successfully."));
    }

    // ─── Transfer ────────────────────────────────────────────────────────────

    @PostMapping("/{tokenId}/transfer/{serialNumber}")
    @Operation(
            summary = "Transfer a single NFT",
            description = "Transfers one NFT instance from one account to another. "
                    + "The sender must hold the NFT and sign the transaction. "
                    + "The recipient must already be associated with the NFT type.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "NFT transferred successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "NFT 0.0.66001 #1 transferred from 0.0.12345 to 0.0.98765 successfully." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Transfer could not be executed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> transferNft(
            @Parameter(description = "The Hedera NFT token type ID.", required = true, example = "0.0.66001")
            @PathVariable("tokenId") String tokenId,
            @Parameter(description = "The serial number of the NFT to transfer.", required = true, example = "1")
            @PathVariable("serialNumber") long serialNumber,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Sender account ID, sender private key, and recipient account ID.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NftTransferRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "fromAccountId": "0.0.12345",
                                      "fromAccountKey": "302e020100300506032b657004220420aabbcc...",
                                      "toAccountId": "0.0.98765"
                                    }""")))
            @RequestBody NftTransferRequest request) throws Exception {
        nftClient.transferNft(
                TokenId.fromString(tokenId),
                serialNumber,
                AccountId.fromString(request.fromAccountId()),
                PrivateKey.fromString(request.fromAccountKey()),
                AccountId.fromString(request.toAccountId()));
        return ResponseEntity.ok(SuccessResponse.of(
                "NFT " + tokenId + " #" + serialNumber
                        + " transferred from " + request.fromAccountId()
                        + " to " + request.toAccountId() + " successfully."));
    }

    @PostMapping("/{tokenId}/transfer")
    @Operation(
            summary = "Transfer multiple NFTs",
            description = "Transfers multiple NFT instances of the same type from one account to another "
                    + "in a single transaction. The sender must hold all specified NFTs. "
                    + "The recipient must already be associated with the NFT type.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "NFTs transferred successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "3 NFT(s) of type 0.0.66001 transferred from 0.0.12345 to 0.0.98765 successfully." }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Serial numbers list is missing or empty.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Transfer could not be executed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> transferNfts(
            @Parameter(description = "The Hedera NFT token type ID.", required = true, example = "0.0.66001")
            @PathVariable("tokenId") String tokenId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Serial numbers, sender account ID + key, and recipient account ID.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NftBatchTransferRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "serialNumbers": [1, 2, 3],
                                      "fromAccountId": "0.0.12345",
                                      "fromAccountKey": "302e020100300506032b657004220420aabbcc...",
                                      "toAccountId": "0.0.98765"
                                    }""")))
            @RequestBody NftBatchTransferRequest request) throws Exception {

        if (request.serialNumbers() == null || request.serialNumbers().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serial numbers must not be empty");
        }

        nftClient.transferNfts(
                TokenId.fromString(tokenId),
                request.serialNumbers(),
                AccountId.fromString(request.fromAccountId()),
                PrivateKey.fromString(request.fromAccountKey()),
                AccountId.fromString(request.toAccountId()));
        return ResponseEntity.ok(SuccessResponse.of(
                request.serialNumbers().size() + " NFT(s) of type " + tokenId
                        + " transferred from " + request.fromAccountId()
                        + " to " + request.toAccountId() + " successfully."));
    }
}
