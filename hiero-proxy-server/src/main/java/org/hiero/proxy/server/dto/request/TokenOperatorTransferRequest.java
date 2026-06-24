package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for transferring fungible tokens from the operator account to another account.")
public record TokenOperatorTransferRequest(
        @Schema(
                description = "The Hedera account ID of the recipient.",
                example = "0.0.98765",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String toAccountId,

        @Schema(
                description = "The number of token units to transfer.",
                example = "250",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        long amount
) {}
