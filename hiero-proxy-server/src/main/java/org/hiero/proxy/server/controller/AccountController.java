package org.hiero.proxy.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hiero.base.data.Account;
import org.hiero.base.AccountClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

import org.hiero.proxy.server.dto.AccountResponse;
import org.hiero.proxy.server.exception.ErrorResponse;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "The ui for all operations regarding account management in a hiero network")
public class AccountController {

    private final AccountClient accountClient;

    public AccountController(AccountClient accountClient) {
        this.accountClient = Objects.requireNonNull(accountClient, "accountClient must not be null");
    }

    @PostMapping
    @Operation(summary = "Create a new Hiero network account", description = "Creates a new Hedera account on the ledger with an initial balance of 0 HBAR. The creation transaction fees are paid by the operator account configured in the proxy server. Returns the newly generated Account ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account successfully created, returning the new Account ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error if the account could not be created on the network", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccountResponse> createAccount() throws Exception {
        Account account = accountClient.createAccount();
        AccountResponse response = new AccountResponse(
                account.accountId().toString(),
                account.publicKey().toString(),
                account.privateKey().toString()
        );
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
    }
}
