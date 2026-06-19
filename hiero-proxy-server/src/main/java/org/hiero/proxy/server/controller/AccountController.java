package org.hiero.proxy.server.controller;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hiero.base.AccountClient;
import org.hiero.base.data.Account;
import org.hiero.base.mirrornode.AccountRepository;
import org.hiero.proxy.server.dto.request.CreateAccountRequest;
import org.hiero.proxy.server.dto.request.DeleteAccountRequest;
import org.hiero.proxy.server.dto.request.DeleteAccountToRecipientRequest;
import org.hiero.proxy.server.dto.request.OperatorTransferRequest;
import org.hiero.proxy.server.dto.request.TransferRequest;
import org.hiero.proxy.server.dto.request.UpdateAccountRequest;
import org.hiero.proxy.server.dto.request.UpdateKeyRequest;
import org.hiero.proxy.server.dto.request.UpdateMemoRequest;
import org.hiero.proxy.server.dto.response.AccountInfoResponse;
import org.hiero.proxy.server.dto.response.AccountResponse;
import org.hiero.proxy.server.dto.response.BalanceResponse;
import org.hiero.proxy.server.dto.response.SuccessResponse;
import org.hiero.proxy.server.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Operations for managing accounts on a Hiero network")
public class AccountController {

    private final AccountClient accountClient;
    private final AccountRepository accountRepository;

    public AccountController(AccountClient accountClient, AccountRepository accountRepository) {
        this.accountClient = Objects.requireNonNull(accountClient, "accountClient must not be null");
        this.accountRepository = Objects.requireNonNull(accountRepository, "accountRepository must not be null");
    }

    // Account creation

    @PostMapping
    @Operation(
            summary = "Create a new account",
            description = "Creates a new Hedera account on the ledger. "
                    + "Provide an optional request body to fund the account with an initial HBAR balance; "
                    + "omit the body to create with 0 HBAR. "
                    + "Transaction fees are paid by the operator account configured in the proxy server.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully.",
                    content = @Content(
                            schema = @Schema(implementation = AccountResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "accountId": "0.0.12345",
                                      "publicKey": "302a300506032b6570032100aabbccddeeff...",
                                      "privateKey": "302e020100300506032b657004220420aabbcc..."
                                    }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid initial balance (e.g. negative value).",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Account could not be created on the network.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccountResponse> createAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Optional initial balance. Omit to create with 0 HBAR.",
                    required = false,
                    content = @Content(
                            schema = @Schema(implementation = CreateAccountRequest.class),
                            examples = @ExampleObject(value = """
                                    { "initialBalanceInHbar": 10 }""")))
            @RequestBody(required = false) CreateAccountRequest request) throws Exception {
        long initialBalance = (request != null) ? request.initialBalanceInHbar() : 0L;
        if (initialBalance < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "initialBalanceInHbar must be >= 0");
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AccountResponse.from(accountClient.createAccount(initialBalance)));
    }

    // Balance queries

    @GetMapping("/{accountId}/balance")
    @Operation(
            summary = "Get account balance",
            description = "Fetches the current HBAR balance of the specified account directly from the Hiero consensus node.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Balance retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = BalanceResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "accountId": "0.0.12345",
                                      "balanceHbar": "10 ℏ"
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve balance.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BalanceResponse> getBalance(
            @Parameter(description = "The Hedera account ID.", required = true, example = "0.0.12345")
            @PathVariable("accountId") String accountId) throws Exception {
        return ResponseEntity.ok(BalanceResponse.from(accountId, accountClient.getAccountBalance(accountId)));
    }

    @GetMapping("/operator/balance")
    @Operation(
            summary = "Get operator balance",
            description = "Fetches the current HBAR balance of the proxy operator account.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Balance retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = BalanceResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "accountId": "operator",
                                      "balanceHbar": "1000 ℏ"
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve operator balance.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BalanceResponse> getOperatorBalance() throws Exception {
        return ResponseEntity.ok(BalanceResponse.from("operator", accountClient.getOperatorAccountBalance()));
    }

    // Account info queries (mirror node)

    @GetMapping("/{accountId}/info")
    @Operation(
            summary = "Get account info",
            description = "Fetches detailed account information from the Hiero mirror node, including "
                    + "EVM address, balance in tinybars, Ethereum nonce, and pending staking reward.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account info retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = AccountInfoResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "accountId": "0.0.12345",
                                      "evmAddress": "0x00000000000000000000000000000000000030d9",
                                      "balanceTinybars": 1000000000,
                                      "ethereumNonce": 0,
                                      "pendingRewardTinybars": 5000000
                                    }"""))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found on the mirror node.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve account info.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccountInfoResponse> getAccountInfo(
            @Parameter(description = "The Hedera account ID.", required = true, example = "0.0.12345")
            @PathVariable("accountId") String accountId) throws Exception {
        return accountRepository.findById(accountId)
                .map(AccountInfoResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account not found: " + accountId));
    }

    // Key rotation

    @PutMapping("/{accountId}/key")
    @Operation(
            summary = "Update account key",
            description = "Replaces the key pair of the specified account with a new private key. "
                    + "The current private key is required to authorise the transaction.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Key updated successfully. Returns the account with its new key pair.",
                    content = @Content(
                            schema = @Schema(implementation = AccountResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "accountId": "0.0.12345",
                                      "publicKey": "302a300506032b6570032100ddeeff...",
                                      "privateKey": "302e020100300506032b657004220420ddeeff..."
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Key could not be updated.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccountResponse> updateKey(
            @Parameter(description = "The Hedera account ID.", required = true, example = "0.0.12345")
            @PathVariable("accountId") String accountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Current private key to authorise the rotation. A fresh key pair is generated server-side and returned.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateKeyRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "currentPrivateKey": "302e020100300506032b657004220420aabbcc..."
                                    }""")))
            @RequestBody UpdateKeyRequest request) throws Exception {
        Account account = Account.of(AccountId.fromString(accountId), PrivateKey.fromString(request.currentPrivateKey()));
        PrivateKey newPrivateKey = PrivateKey.generateED25519();
        return ResponseEntity.ok(AccountResponse.from(
                accountClient.updateAccountKey(account, newPrivateKey)));
    }

    // Memo update

    @PutMapping("/{accountId}/memo")
    @Operation(
            summary = "Update account memo",
            description = "Updates the memo field of the specified account. "
                    + "The account's current private key is required to authorise the transaction.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Memo updated successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Account memo updated successfully." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Memo could not be updated.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> updateMemo(
            @Parameter(description = "The Hedera account ID.", required = true, example = "0.0.12345")
            @PathVariable("accountId") String accountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Private key and new memo value.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateMemoRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "privateKey": "302e020100300506032b657004220420aabbcc...",
                                      "memo": "Updated enterprise account memo"
                                    }""")))
            @RequestBody UpdateMemoRequest request) throws Exception {
        Account account = Account.of(AccountId.fromString(accountId), PrivateKey.fromString(request.privateKey()));
        accountClient.updateAccountMemo(account, request.memo());
        return ResponseEntity.ok(SuccessResponse.of("Account memo updated successfully."));
    }

    // Atomic key rotation and memo update

    @PutMapping("/{accountId}")
    @Operation(
            summary = "Update account key and memo",
            description = "Updates both the key pair and memo of the specified account in a single network transaction. "
                    + "More efficient than calling the key and memo endpoints separately.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account updated successfully. Returns the account with its new key pair.",
                    content = @Content(
                            schema = @Schema(implementation = AccountResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "accountId": "0.0.12345",
                                      "publicKey": "302a300506032b6570032100ddeeff...",
                                      "privateKey": "302e020100300506032b657004220420ddeeff..."
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Account could not be updated.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccountResponse> updateAccount(
            @Parameter(description = "The Hedera account ID.", required = true, example = "0.0.12345")
            @PathVariable("accountId") String accountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Current private key and new memo. A fresh key pair is generated server-side and returned.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateAccountRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "currentPrivateKey": "302e020100300506032b657004220420aabbcc...",
                                      "memo": "Updated enterprise account"
                                    }""")))
            @RequestBody UpdateAccountRequest request) throws Exception {
        Account account = Account.of(AccountId.fromString(accountId), PrivateKey.fromString(request.currentPrivateKey()));
        PrivateKey newPrivateKey = PrivateKey.generateED25519();
        return ResponseEntity.ok(AccountResponse.from(
                accountClient.updateAccount(account, newPrivateKey, request.memo())));
    }

    // Account deletion — remaining balance transferred to the operator

    @DeleteMapping("/{accountId}")
    @Operation(
            summary = "Delete account",
            description = "Deletes the specified account from the ledger. "
                    + "Any remaining balance is automatically transferred to the proxy operator account.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account deleted successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Account 0.0.12345 deleted successfully. Remaining balance transferred to operator." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Account could not be deleted.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> deleteAccount(
            @Parameter(description = "The Hedera account ID to delete.", required = true, example = "0.0.12345")
            @PathVariable("accountId") String accountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Private key of the account to be deleted.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = DeleteAccountRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "privateKey": "302e020100300506032b657004220420aabbcc..."
                                    }""")))
            @RequestBody DeleteAccountRequest request) throws Exception {
        Account account = Account.of(AccountId.fromString(accountId), PrivateKey.fromString(request.privateKey()));
        accountClient.deleteAccount(account);
        return ResponseEntity.ok(SuccessResponse.of(
                "Account " + accountId + " deleted successfully. Remaining balance transferred to operator."));
    }

    // Account deletion — remaining balance transferred to a specified recipient

    @DeleteMapping("/{accountId}/to/{recipientAccountId}")
    @Operation(
            summary = "Delete account and transfer balance to recipient",
            description = "Deletes the specified account from the ledger and transfers any remaining balance "
                    + "to the specified recipient account instead of the operator.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account deleted and balance transferred successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Account 0.0.12345 deleted successfully. Remaining balance transferred to 0.0.98765." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Account could not be deleted.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> deleteAccountToRecipient(
            @Parameter(description = "The Hedera account ID to delete.", required = true, example = "0.0.12345")
            @PathVariable("accountId") String accountId,
            @Parameter(description = "The Hedera account ID that receives the remaining balance.", required = true, example = "0.0.98765")
            @PathVariable("recipientAccountId") String recipientAccountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Private keys of both the account being deleted and the recipient.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = DeleteAccountToRecipientRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "accountPrivateKey": "302e020100300506032b657004220420aabbcc...",
                                      "recipientPrivateKey": "302e020100300506032b657004220420ddeeff..."
                                    }""")))
            @RequestBody DeleteAccountToRecipientRequest request) throws Exception {
        Account account = Account.of(AccountId.fromString(accountId), PrivateKey.fromString(request.accountPrivateKey()));
        Account recipient = Account.of(AccountId.fromString(recipientAccountId), PrivateKey.fromString(request.recipientPrivateKey()));
        accountClient.deleteAccount(account, recipient);
        return ResponseEntity.ok(SuccessResponse.of(
                "Account " + accountId + " deleted successfully. Remaining balance transferred to " + recipientAccountId + "."));
    }

    // HBAR transfer initiated by the operator

    @PostMapping("/transfer")
    @Operation(
            summary = "Transfer HBAR from operator",
            description = "Transfers HBAR from the proxy operator account to a target account. "
                    + "The operator's private key is used automatically — no key is required in the request.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer completed successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Successfully transferred 100 HBAR from operator to 0.0.98765." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Transfer could not be executed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> operatorTransfer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Target account ID and amount to transfer.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OperatorTransferRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "toAccountId": "0.0.98765",
                                      "amountInHbar": 100
                                    }""")))
            @RequestBody OperatorTransferRequest request) throws Exception {
        accountClient.transferHbar(request.toAccountId(), Hbar.from(request.amountInHbar()));
        return ResponseEntity.ok(SuccessResponse.of(
                "Successfully transferred " + request.amountInHbar() + " HBAR from operator to " + request.toAccountId() + "."));
    }

    // HBAR transfer between user accounts

    @PostMapping("/{accountId}/transfer")
    @Operation(
            summary = "Transfer HBAR between accounts",
            description = "Transfers HBAR from the specified account to a target account. "
                    + "The sender's private key is required to sign the transaction.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer completed successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Successfully transferred 50 HBAR from 0.0.12345 to 0.0.98765." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Transfer could not be executed.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> transfer(
            @Parameter(description = "The Hedera account ID of the sender.", required = true, example = "0.0.12345")
            @PathVariable("accountId") String accountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Sender's private key, recipient account ID, and amount.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TransferRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "fromAccountPrivateKey": "302e020100300506032b657004220420aabbcc...",
                                      "toAccountId": "0.0.98765",
                                      "amountInHbar": 50
                                    }""")))
            @RequestBody TransferRequest request) throws Exception {
        accountClient.transferHbar(accountId, request.fromAccountPrivateKey(), request.toAccountId(), request.amountInHbar());
        return ResponseEntity.ok(SuccessResponse.of(
                "Successfully transferred " + request.amountInHbar() + " HBAR from " + accountId + " to " + request.toAccountId() + "."));
    }
}
