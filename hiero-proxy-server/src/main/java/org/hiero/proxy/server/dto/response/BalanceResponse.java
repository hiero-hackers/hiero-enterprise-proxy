package org.hiero.proxy.server.dto.response;

import com.hedera.hashgraph.sdk.Hbar;
import io.swagger.v3.oas.annotations.media.Schema;

public record BalanceResponse(
        @Schema(description = "The account ID", example = "0.0.12345")
        String accountId,

        @Schema(description = "The balance of the account in HBAR", example = "10.5")
        String balanceHbar
) {
    /** Convenience factory that converts an SDK {@link Hbar} value to its string representation. */
    public static BalanceResponse from(String accountId, Hbar balance) {
        return new BalanceResponse(accountId, balance.toString());
    }
}
