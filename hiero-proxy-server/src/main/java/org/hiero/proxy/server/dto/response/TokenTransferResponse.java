package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hiero.base.data.TokenTransfer;

@Schema(description = "A fungible token transfer entry within a transaction.")
public record TokenTransferResponse(
        @Schema(description = "The Hedera token ID.", example = "0.0.55001")
        String tokenId,

        @Schema(description = "The Hedera account ID involved in this transfer.", example = "0.0.12345")
        String accountId,

        @Schema(description = "Amount of token units transferred. Positive = credit, negative = debit.",
                example = "500")
        long amount,

        @Schema(description = "Whether this transfer was an approved allowance transfer.", example = "false")
        boolean isApproval
) {
    public static TokenTransferResponse from(TokenTransfer t) {
        return new TokenTransferResponse(
                t.tokenId().toString(),
                t.account().toString(),
                t.amount(),
                t.isApproval()
        );
    }
}
