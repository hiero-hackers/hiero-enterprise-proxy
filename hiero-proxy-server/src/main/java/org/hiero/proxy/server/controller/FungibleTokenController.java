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
import org.hiero.base.FungibleTokenClient;
import org.hiero.base.mirrornode.TokenRepository;
import org.hiero.proxy.server.dto.request.BurnTokenRequest;
import org.hiero.proxy.server.dto.request.CreateTokenRequest;
import org.hiero.proxy.server.dto.request.MintTokenRequest;
import org.hiero.proxy.server.dto.request.TokenAssociateRequest;
import org.hiero.proxy.server.dto.request.TokenBatchAssociateRequest;
import org.hiero.proxy.server.dto.request.TokenOperatorTransferRequest;
import org.hiero.proxy.server.dto.request.TokenTransferRequest;
import org.hiero.proxy.server.dto.response.SuccessResponse;
import org.hiero.proxy.server.dto.response.TokenCreatedResponse;
import org.hiero.proxy.server.dto.response.TokenInfoResponse;
import org.hiero.proxy.server.dto.response.TokenSupplyResponse;
import org.hiero.proxy.server.dto.response.TokenBalanceResponse;
import org.hiero.proxy.server.dto.response.TokenResponse;
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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tokens")
@Tag(name = "Fungible Tokens", description = "Operations for managing fungible tokens (HTS) on a Hiero network")
public class FungibleTokenController {

    private final FungibleTokenClient tokenClient;
    private final TokenRepository tokenRepository;

    public FungibleTokenController(FungibleTokenClient tokenClient, TokenRepository tokenRepository) {
        this.tokenClient = Objects.requireNonNull(tokenClient, "tokenClient must not be null");
        this.tokenRepository = Objects.requireNonNull(tokenRepository, "tokenRepository must not be null");
    }

    // Token Creation

    @PostMapping
    @Operation(
            summary = "Create a fungible token",
            description = "Creates a new fungible token on the Hiero Token Service (HTS). "
                    + "The operator account is used as treasury and supply account by default. "
                    + "Provide optional `treasuryAccountId` + `treasuryKey` to use a custom treasury account, "
                    + "and/or `supplyKey` to use a custom supply key for future mint/burn operations.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Token created successfully.",
                    content = @Content(
                            schema = @Schema(implementation = TokenCreatedResponse.class),
                            examples = @ExampleObject(value = """
                                    { "tokenId": "0.0.55001" }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request — name or symbol is missing.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Token could not be created on the network.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TokenCreatedResponse> createToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Token definition. Only `name` and `symbol` are required; "
                            + "all key fields are optional.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateTokenRequest.class),
                            examples = @ExampleObject(value = """
                                    { "name": "My Enterprise Token", "symbol": "MET" }""")))
            @RequestBody CreateTokenRequest request) throws Exception {

        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token name must not be blank");
        }
        if (request.symbol() == null || request.symbol().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token symbol must not be blank");
        }

        final TokenId tokenId;

        boolean hasTreasury = request.treasuryAccountId() != null && request.treasuryKey() != null;
        boolean hasSupplyKey = request.supplyKey() != null;

        if (hasTreasury && hasSupplyKey) {
            // Custom treasury + custom supply key
            tokenId = tokenClient.createToken(
                    request.name(),
                    request.symbol(),
                    request.treasuryAccountId(),
                    request.treasuryKey(),
                    request.supplyKey());
        } else if (hasTreasury) {
            // Custom treasury, operator as supply
            tokenId = tokenClient.createToken(
                    request.name(),
                    request.symbol(),
                    request.treasuryAccountId(),
                    request.treasuryKey());
        } else if (hasSupplyKey) {
            // Operator as treasury, custom supply key
            tokenId = tokenClient.createToken(
                    request.name(),
                    request.symbol(),
                    request.supplyKey());
        } else {
            // Operator as both treasury and supply
            tokenId = tokenClient.createToken(request.name(), request.symbol());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(TokenCreatedResponse.of(tokenId));
    }

    // Token Info (mirror node)

    @GetMapping("/{tokenId}")
    @Operation(
            summary = "Get token info",
            description = "Fetches detailed token information from the Hiero mirror node, "
                    + "including name, symbol, supply, treasury, and type.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token info retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = TokenInfoResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "tokenId": "0.0.55001",
                                      "name": "My Enterprise Token",
                                      "symbol": "MET",
                                      "type": "FUNGIBLE_COMMON",
                                      "memo": null,
                                      "decimals": 0,
                                      "supplyType": "INFINITE",
                                      "totalSupply": "5000",
                                      "maxSupply": "0",
                                      "treasuryAccountId": "0.0.1001",
                                      "deleted": false
                                    }"""))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Token not found on the mirror node.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve token info.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TokenInfoResponse> getTokenInfo(
            @Parameter(description = "The Hedera token ID.", required = true, example = "0.0.55001")
            @PathVariable("tokenId") String tokenId) throws Exception {
        return tokenRepository.findById(tokenId)
                .map(TokenInfoResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Token not found: " + tokenId));
    }

    @GetMapping("/{tokenId}/balances/{accountId}")
    @Operation(
            summary = "Get token balance",
            description = "Fetches the token balance for a specific account from the Hiero mirror node.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token balance retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = TokenBalanceResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "accountId": "0.0.12345",
                                      "balance": 250,
                                      "decimals": 0
                                    }"""))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Balance not found on the mirror node.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve balance info.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TokenBalanceResponse> getTokenBalance(
            @Parameter(description = "The Hedera token ID.", required = true, example = "0.0.55001")
            @PathVariable("tokenId") String tokenId,
            @Parameter(description = "The Hedera account ID.", required = true, example = "0.0.12345")
            @PathVariable("accountId") String accountId) throws Exception {
        return tokenRepository.getBalancesForAccount(tokenId, accountId)
                .getData()
                .stream()
                .findFirst()
                .map(TokenBalanceResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Balance not found for token: " + tokenId + " and account: " + accountId));
    }

    @GetMapping("/{tokenId}/balances")
    @Operation(
            summary = "Get all token balances",
            description = "Fetches a list of balances for all accounts that hold the specified token from the Hiero mirror node.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token balances retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = TokenBalanceResponse.class, type = "array"))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve balance info.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TokenBalanceResponse>> getTokenBalances(
            @Parameter(description = "The Hedera token ID.", required = true, example = "0.0.55001")
            @PathVariable("tokenId") String tokenId) throws Exception {
        return ResponseEntity.ok(
                tokenRepository.getBalances(tokenId)
                        .getData()
                        .stream()
                        .map(TokenBalanceResponse::from)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/account/{accountId}")
    @Operation(
            summary = "Get tokens by account",
            description = "Fetches a list of tokens associated with the specified account from the Hiero mirror node.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = TokenResponse.class, type = "array"))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve token info.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TokenResponse>> getAccountTokens(
            @Parameter(description = "The Hedera account ID.", required = true, example = "0.0.12345")
            @PathVariable("accountId") String accountId) throws Exception {
        return ResponseEntity.ok(
                tokenRepository.findByAccount(accountId)
                        .getData()
                        .stream()
                        .map(TokenResponse::from)
                        .collect(Collectors.toList())
        );
    }

    // Associate / Dissociate

    @PostMapping("/{tokenId}/associate")
    @Operation(
            summary = "Associate account with token",
            description = "Associates a Hedera account with this token so it can hold and receive token units. "
                    + "An account must be associated before it can receive a transfer of this token.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account associated with token successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Account 0.0.12345 associated with token 0.0.55001 successfully." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Association could not be executed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> associateToken(
            @Parameter(description = "The Hedera token ID.", required = true, example = "0.0.55001")
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
        tokenClient.associateToken(
                TokenId.fromString(tokenId),
                request.accountId(),
                request.accountKey());
        return ResponseEntity.ok(SuccessResponse.of(
                "Account " + request.accountId() + " associated with token " + tokenId + " successfully."));
    }

    @DeleteMapping("/{tokenId}/associate")
    @Operation(
            summary = "Dissociate account from token",
            description = "Removes the association between a Hedera account and this token. "
                    + "The account's token balance must be zero before dissociating.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account dissociated from token successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Account 0.0.12345 dissociated from token 0.0.55001 successfully." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Dissociation could not be executed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> dissociateToken(
            @Parameter(description = "The Hedera token ID.", required = true, example = "0.0.55001")
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
        tokenClient.dissociateToken(
                tokenId,
                request.accountId(),
                request.accountKey());
        return ResponseEntity.ok(SuccessResponse.of(
                        "Account " + request.accountId() + " dissociated from token " + tokenId + " successfully."));
    }

    @PostMapping("/associate")
    @Operation(
            summary = "Batch associate account with multiple tokens",
            description = "Associates a Hedera account with multiple tokens at once so it can hold and receive token units.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account associated with tokens successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Account 0.0.12345 associated with 2 tokens successfully." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Association could not be executed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> batchAssociateTokens(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of Token IDs, Account ID, and private key to authorise the association.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TokenBatchAssociateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "tokenIds": ["0.0.55001", "0.0.55002"],
                                      "accountId": "0.0.12345",
                                      "accountKey": "302e020100300506032b657004220420aabbcc..."
                                    }""")))
            @RequestBody TokenBatchAssociateRequest request) throws Exception {
        List<TokenId> tokenIds = request.tokenIds().stream()
                .map(TokenId::fromString)
                .collect(Collectors.toList());
        tokenClient.associateToken(
                tokenIds,
                AccountId.fromString(request.accountId()),
                PrivateKey.fromString(request.accountKey()));
        return ResponseEntity.ok(SuccessResponse.of(
                "Account " + request.accountId() + " associated with " + tokenIds.size() + " tokens successfully."));
    }

    @DeleteMapping("/associate")
    @Operation(
            summary = "Batch dissociate account from multiple tokens",
            description = "Removes the association between a Hedera account and multiple tokens at once.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account dissociated from tokens successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Account 0.0.12345 dissociated from 2 tokens successfully." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Dissociation could not be executed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> batchDissociateTokens(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of Token IDs, Account ID, and private key to authorise the dissociation.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TokenBatchAssociateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "tokenIds": ["0.0.55001", "0.0.55002"],
                                      "accountId": "0.0.12345",
                                      "accountKey": "302e020100300506032b657004220420aabbcc..."
                                    }""")))
            @RequestBody TokenBatchAssociateRequest request) throws Exception {
        List<TokenId> tokenIds = request.tokenIds().stream()
                .map(TokenId::fromString)
                .collect(Collectors.toList());
        tokenClient.dissociateToken(
                tokenIds,
                AccountId.fromString(request.accountId()),
                PrivateKey.fromString(request.accountKey()));
        return ResponseEntity.ok(SuccessResponse.of(
                "Account " + request.accountId() + " dissociated from " + tokenIds.size() + " tokens successfully."));
    }

    // Mint

    @PostMapping("/{tokenId}/mint")
    @Operation(
            summary = "Mint token units",
            description = "Mints new units of a fungible token and adds them to the treasury account's balance. "
                    + "Omit `supplyKey` if the operator account is the supply account.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens minted successfully. Returns the updated total supply.",
                    content = @Content(
                            schema = @Schema(implementation = TokenSupplyResponse.class),
                            examples = @ExampleObject(value = """
                                    { "totalSupply": 6000 }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid mint amount (must be > 0).",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Mint operation failed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TokenSupplyResponse> mintToken(
            @Parameter(description = "The Hedera token ID.", required = true, example = "0.0.55001")
            @PathVariable("tokenId") String tokenId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Amount to mint and optional supply key.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = MintTokenRequest.class),
                            examples = @ExampleObject(value = """
                                    { "amount": 1000 }""")))
            @RequestBody MintTokenRequest request) throws Exception {
        if (request.amount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mint amount must be > 0");
        }

        final long newSupply;
        if (request.supplyKey() != null) {
            newSupply = tokenClient.mintToken(
                    TokenId.fromString(tokenId),
                    PrivateKey.fromString(request.supplyKey()),
                    request.amount());
        } else {
            newSupply = tokenClient.mintToken(TokenId.fromString(tokenId), request.amount());
        }
        return ResponseEntity.ok(TokenSupplyResponse.of(newSupply));
    }

    // Burn

    @PostMapping("/{tokenId}/burn")
    @Operation(
            summary = "Burn token units",
            description = "Permanently removes fungible token units from the treasury account's balance. "
                    + "Omit `supplyKey` if the operator account is the supply account.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens burned successfully. Returns the updated total supply.",
                    content = @Content(
                            schema = @Schema(implementation = TokenSupplyResponse.class),
                            examples = @ExampleObject(value = """
                                    { "totalSupply": 4500 }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid burn amount (must be > 0).",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Burn operation failed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TokenSupplyResponse> burnToken(
            @Parameter(description = "The Hedera token ID.", required = true, example = "0.0.55001")
            @PathVariable("tokenId") String tokenId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Amount to burn and optional supply key.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = BurnTokenRequest.class),
                            examples = @ExampleObject(value = """
                                    { "amount": 500 }""")))
            @RequestBody BurnTokenRequest request) throws Exception {
        if (request.amount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Burn amount must be > 0");
        }

        final long newSupply;
        if (request.supplyKey() != null) {
            newSupply = tokenClient.burnToken(
                    TokenId.fromString(tokenId),
                    request.amount(),
                    request.supplyKey());
        } else {
            newSupply = tokenClient.burnToken(TokenId.fromString(tokenId), request.amount());
        }
        return ResponseEntity.ok(TokenSupplyResponse.of(newSupply));
    }

    // Transfer from Operator

    @PostMapping("/{tokenId}/transfer")
    @Operation(
            summary = "Transfer tokens from operator",
            description = "Transfers fungible token units from the proxy operator account to a target account. "
                    + "The operator's key is used automatically — no key is required in the request. "
                    + "The recipient account must already be associated with this token.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer completed successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Successfully transferred 250 units of 0.0.55001 from operator to 0.0.98765." }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid transfer amount (must be > 0).",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Transfer could not be executed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> transferFromOperator(
            @Parameter(description = "The Hedera token ID.", required = true, example = "0.0.55001")
            @PathVariable("tokenId") String tokenId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Recipient account ID and amount to transfer.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TokenOperatorTransferRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "toAccountId": "0.0.98765",
                                      "amount": 250
                                    }""")))
            @RequestBody TokenOperatorTransferRequest request) throws Exception {
        if (request.amount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer amount must be > 0");
        }
        tokenClient.transferToken(
                TokenId.fromString(tokenId),
                AccountId.fromString(request.toAccountId()),
                request.amount());
        return ResponseEntity.ok(SuccessResponse.of(
                "Successfully transferred " + request.amount()
                        + " units of " + tokenId
                        + " from operator to " + request.toAccountId() + "."));
    }

    // Transfer between User Accounts

    @PostMapping("/{tokenId}/transfer/{fromAccountId}")
    @Operation(
            summary = "Transfer tokens between accounts",
            description = "Transfers fungible token units from the specified sender account to a target account. "
                    + "The sender's private key is required to sign the transaction. "
                    + "Both accounts must already be associated with this token.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer completed successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Successfully transferred 100 units of 0.0.55001 from 0.0.12345 to 0.0.98765." }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid transfer amount (must be > 0).",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Transfer could not be executed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> transferBetweenAccounts(
            @Parameter(description = "The Hedera token ID.", required = true, example = "0.0.55001")
            @PathVariable("tokenId") String tokenId,
            @Parameter(description = "The Hedera account ID of the sender.", required = true, example = "0.0.12345")
            @PathVariable("fromAccountId") String fromAccountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Sender's private key, recipient account ID, and amount.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TokenTransferRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "fromAccountKey": "302e020100300506032b657004220420aabbcc...",
                                      "toAccountId": "0.0.98765",
                                      "amount": 100
                                    }""")))
            @RequestBody TokenTransferRequest request) throws Exception {
        if (request.amount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer amount must be > 0");
        }
        tokenClient.transferToken(
                TokenId.fromString(tokenId),
                fromAccountId,
                request.fromAccountKey(),
                request.toAccountId(),
                request.amount());
        return ResponseEntity.ok(SuccessResponse.of(
                "Successfully transferred " + request.amount()
                        + " units of " + tokenId
                        + " from " + fromAccountId
                        + " to " + request.toAccountId() + "."));
    }
}
