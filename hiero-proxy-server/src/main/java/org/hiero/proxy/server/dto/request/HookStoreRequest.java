package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request body for storing or updating a hook on the Hiero network.")
public record HookStoreRequest(
        @Schema(
                description = "The ID of the contract the hook is attached to.",
                example = "0.0.12345",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String contractId,

        @Schema(
                description = "The number of the hook.",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long hookNumber,

        @Schema(
                description = "List of ED25519 or ECDSA private keys (hex encoded) required to sign the transaction.",
                example = "[\"302e020100300506032b657004220420...\", \"302e020100300506032b657004220420...\"]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> signerKeys
) {}
