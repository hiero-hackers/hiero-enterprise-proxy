package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateMemoRequest(
        @Schema(description = "The current private key to authorize the update", example = "302e020100300506032b657004220420...") 
        String privateKey,

        @Schema(description = "The new memo string to set on the account", example = "Updated enterprise account memo") 
        String memo
) {}
