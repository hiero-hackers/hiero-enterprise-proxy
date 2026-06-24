package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hiero.base.data.Balance;

@Schema(description = "Response containing the balance of a specific token for an account.")
public record TokenBalanceResponse(
        @Schema(description = "The account ID.", example = "0.0.12345")
        String accountId,

        @Schema(description = "The token balance amount.", example = "250")
        long balance,

        @Schema(description = "The number of decimals the token has.", example = "0")
        long decimals
) {
    public static TokenBalanceResponse from(Balance b) {
        return new TokenBalanceResponse(
                b.accountId().toString(),
                b.balance(),
                b.decimals()
        );
    }
}
