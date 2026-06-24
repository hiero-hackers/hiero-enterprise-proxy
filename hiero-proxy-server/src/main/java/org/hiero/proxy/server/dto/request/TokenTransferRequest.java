package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for transferring fungible tokens between two user accounts. "
        + "The sender's private key is required to authorise the transaction.")
public record TokenTransferRequest(
        @Schema(
                description = "The private key of the sending account.",
                example = "302e020100300506032b657004220420aabbcc...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String fromAccountKey,

        @Schema(
                description = "The Hedera account ID of the recipient.",
                example = "0.0.98765",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String toAccountId,

        @Schema(
                description = "The number of token units to transfer.",
                example = "100",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        long amount
) {}
