package org.hiero.proxy.server.controller;

import org.hiero.base.data.Account;
import org.hiero.base.AccountClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountClient accountClient;

    public AccountController(AccountClient accountClient) {
        this.accountClient = Objects.requireNonNull(accountClient, "accountClient must not be null");
    }

    @PostMapping
    public Map<String, Object> createAccount() {
        try {
            Account account = accountClient.createAccount();
            return Map.of(
                    "status", "success",
                    "accountId", account.accountId(),
                    "message", "Account created successfully"
            );
        } catch (Exception e) {
            throw new RuntimeException("Error creating account", e);
        }
    }
}
