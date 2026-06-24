package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for minting new units of a fungible token.")
public record MintTokenRequest(
        @Schema(
                description = "The number of token units to mint and add to the treasury account's balance.",
                example = "1000",
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
