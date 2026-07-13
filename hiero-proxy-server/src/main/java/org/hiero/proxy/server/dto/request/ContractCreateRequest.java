package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating a new smart contract. "
        + "The bytecode must be provided as a Hex-encoded string (with or without '0x' prefix).")
public record ContractCreateRequest(
        @Schema(
                description = "Hex-encoded bytecode of the smart contract.",
                example = "6080604052348015610010576000...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String bytecode
) {}
