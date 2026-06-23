package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating a private topic with a custom admin key. "
        + "The provided admin key controls topic management. "
        + "The server generates a fresh ED25519 submit key and returns it in the response.")
public record CreatePrivateTopicWithAdminKeyRequest(
        @Schema(
                description = "The private key to use as the admin key for the topic.",
                example = "302e020100300506032b657004220420aabbcc...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String adminPrivateKey,

        @Schema(
                description = "Optional memo to attach to the topic.",
                example = "Private enterprise channel",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String memo
) {}
