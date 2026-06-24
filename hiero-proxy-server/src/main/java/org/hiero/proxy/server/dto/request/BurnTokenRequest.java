package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for burning (permanently removing) fungible token units.")
public record BurnTokenRequest(
        @Schema(
                description = "The number of token units to burn from the treasury account.",
                example = "500",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        long amount,

        @Schema(
                description = "Private key of the supply account. "
                        + "Required only when the token was created with a custom supply key; "
                        + "omit when the operator is the supply account.",
                example = "302e020100300506032b657004220420aabbcc...",
                nullable = true)
        String supplyKey
) {}
