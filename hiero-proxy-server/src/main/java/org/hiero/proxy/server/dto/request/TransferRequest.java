package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record TransferRequest(
        @Schema(description = "The private key of the sending account", example = "302e020100300506032b657004220420...") 
        String fromAccountPrivateKey,

        @Schema(description = "The account ID receiving the HBAR", example = "0.0.98765") 
        String toAccountId,

        @Schema(description = "The amount of HBAR to transfer", example = "50.5") 
        long amountInHbar
) {}
