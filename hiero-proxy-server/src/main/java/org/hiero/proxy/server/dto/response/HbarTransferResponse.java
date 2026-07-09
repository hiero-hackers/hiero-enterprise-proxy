package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hiero.base.data.Transfer;

@Schema(description = "An HBAR transfer entry within a transaction.")
public record HbarTransferResponse(
        @Schema(description = "The Hedera account ID involved in this transfer.", example = "0.0.12345")
        String accountId,

        @Schema(description = "Amount transferred in tinybar. Positive = credit, negative = debit.",
                example = "-1000000000")
        long amount,

        @Schema(description = "Whether this transfer was an approved allowance transfer.", example = "false")
        boolean isApproval
) {
    public static HbarTransferResponse from(Transfer t) {
        return new HbarTransferResponse(t.account().toString(), t.amount(), t.isApproval());
    }
}
