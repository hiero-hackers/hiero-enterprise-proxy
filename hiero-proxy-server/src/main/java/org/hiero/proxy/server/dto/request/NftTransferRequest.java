package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for transferring a single NFT from one account to another.")
public record NftTransferRequest(
        @Schema(
                description = "The Hedera account ID that currently holds the NFT.",
                example = "0.0.12345",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String fromAccountId,

        @Schema(
                description = "The private key of the sending account to authorise the transfer.",
                example = "302e020100300506032b657004220420aabbcc...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String fromAccountKey,

        @Schema(
                description = "The Hedera account ID that should receive the NFT. "
                        + "The recipient must already be associated with the NFT type.",
                example = "0.0.98765",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String toAccountId
) {}
