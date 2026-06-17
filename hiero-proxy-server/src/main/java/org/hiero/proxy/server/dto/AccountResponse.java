package org.hiero.proxy.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AccountResponse(
        @Schema(description = "The newly generated Hedera Account ID", example = "0.0.12345") 
        String accountId,

        @Schema(description = "The public key associated with the account", example = "302a300506032b6570032100...") 
        String publicKey,

        @Schema(description = "The private key to control the account (keep this secure!)", example = "302e020100300506032b657004220420...") 
        String privateKey
) {}
