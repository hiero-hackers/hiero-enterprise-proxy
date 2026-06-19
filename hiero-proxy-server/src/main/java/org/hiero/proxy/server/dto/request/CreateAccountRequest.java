package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Optional request body for account creation. If omitted the account is created with 0 HBAR.")
public record CreateAccountRequest(
        @Schema(
                description = "Initial balance to fund the new account with (in HBAR). Must be >= 0.",
                example = "10",
                minimum = "0")
        long initialBalanceInHbar
) {}
