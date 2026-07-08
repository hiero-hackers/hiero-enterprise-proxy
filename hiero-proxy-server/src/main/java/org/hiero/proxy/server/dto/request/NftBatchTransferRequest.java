package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Request body for transferring multiple NFT serial numbers of the same type "
        + "from one account to another in a single transaction.")
public record NftBatchTransferRequest(
        @Schema(
                description = "The serial numbers of the NFTs to transfer.",
                example = "[1, 2, 3]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<Long> serialNumbers,

        @Schema(
                description = "The Hedera account ID that currently holds the NFTs.",
                example = "0.0.12345",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String fromAccountId,

        @Schema(
                description = "The private key of the sending account to authorise the transfer.",
                example = "302e020100300506032b657004220420aabbcc...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String fromAccountKey,

        @Schema(
                description = "The Hedera account ID that should receive the NFTs. "
                        + "The recipient must already be associated with the NFT type.",
                example = "0.0.98765",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String toAccountId
) {}
