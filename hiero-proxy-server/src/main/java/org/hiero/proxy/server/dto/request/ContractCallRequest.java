package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for calling a smart contract function.")
public record ContractCallRequest(
        @Schema(
                description = "The name of the function to call.",
                example = "myFunction",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String functionName
) {}
