package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating a new NFT token type. "
        + "The operator account acts as both treasury and supply account when no custom keys are provided.")
public record CreateNftTypeRequest(
        @Schema(
                description = "The full name of the NFT type.",
                example = "My Enterprise NFT",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @Schema(
                description = "The ticker symbol of the NFT type.",
                example = "MENFT",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String symbol,

        @Schema(
                description = "Account ID of the treasury account. "
                        + "If omitted, the operator account is used as treasury.",
                example = "0.0.12345",
                nullable = true)
        String treasuryAccountId,

        @Schema(
                description = "Private key of the treasury account. "
                        + "Required when treasuryAccountId is set.",
                example = "302e020100300506032b657004220420aabbcc...",
                nullable = true)
        String treasuryKey,

        @Schema(
                description = "Private key of the supply account. "
                        + "If omitted, the operator account is used as supply (mint/burn) account.",
                example = "302e020100300506032b657004220420ddeeff...",
                nullable = true)
        String supplierKey
) {}
