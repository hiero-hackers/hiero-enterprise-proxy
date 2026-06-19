package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record OperatorTransferRequest(
        @Schema(description = "The account ID receiving the HBAR from the operator", example = "0.0.98765") 
        String toAccountId,

        @Schema(description = "The amount of HBAR to transfer", example = "100") 
        long amountInHbar
) {}
