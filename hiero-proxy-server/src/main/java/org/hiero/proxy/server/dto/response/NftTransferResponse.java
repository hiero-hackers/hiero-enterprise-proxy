package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hiero.base.data.NftTransfer;

@Schema(description = "An NFT transfer entry within a transaction.")
public record NftTransferResponse(
        @Schema(description = "The Hedera NFT token type ID.", example = "0.0.66001", nullable = true)
        String tokenId,

        @Schema(description = "Serial number of the transferred NFT.", example = "1")
        long serialNumber,

        @Schema(description = "The account ID that sent the NFT.", example = "0.0.12345", nullable = true)
        String senderAccountId,

        @Schema(description = "The account ID that received the NFT.", example = "0.0.98765", nullable = true)
        String receiverAccountId,

        @Schema(description = "Whether this transfer was an approved allowance transfer.", example = "false")
        boolean isApproval
) {
    public static NftTransferResponse from(NftTransfer t) {
        return new NftTransferResponse(
                t.tokenId()          != null ? t.tokenId().toString()          : null,
                t.serialNumber(),
                t.senderAccountId()  != null ? t.senderAccountId().toString()  : null,
                t.receiverAccountId() != null ? t.receiverAccountId().toString() : null,
                t.isApproval()
        );
    }
}
