package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for rotating an account's key pair. "
        + "The proxy generates a fresh key pair server-side and returns it in the response — "
        + "you only need to prove ownership of the current key.")
public record UpdateKeyRequest(
        @Schema(
                description = "The current private key of the account, used to authorise the key rotation.",
                example = "302e020100300506032b657004220420aabbcc...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String currentPrivateKey
) {}
