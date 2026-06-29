package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for associating or dissociating an account with a fungible token.")
public record TokenAssociateRequest(
        @Schema(
                description = "The Hedera account ID to associate with the token.",
                example = "0.0.12345",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String accountId,

        @Schema(
                description = "The private key of the account to authorise the association.",
                example = "302e020100300506032b657004220420aabbcc...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String accountKey
) {}
