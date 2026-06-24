package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating a new fungible token. "
        + "The operator account acts as both treasury and supply account when no custom keys are provided.")
public record CreateTokenRequest(
        @Schema(
                description = "The full name of the token.",
                example = "My Enterprise Token",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @Schema(
                description = "The ticker symbol of the token.",
                example = "MET",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String symbol,

        @Schema(
                description = "Private key of the treasury account. "
                        + "If omitted, the operator account is used as treasury.",
                example = "302e020100300506032b657004220420aabbcc...",
                nullable = true)
        String treasuryAccountId,

        @Schema(
                description = "Account ID of the treasury account. "
                        + "Required when treasuryAccountId is set.",
                example = "0.0.12345",
                nullable = true)
        String treasuryKey,

        @Schema(
                description = "Private key of the supply account. "
                        + "If omitted, the operator account is used as supply account.",
                example = "302e020100300506032b657004220420ddeeff...",
                nullable = true)
        String supplyKey
) {}
