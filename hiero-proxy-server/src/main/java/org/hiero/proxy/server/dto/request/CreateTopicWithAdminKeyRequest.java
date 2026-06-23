package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating a public topic with a custom admin key. "
        + "The provided private key becomes the admin key for the topic instead of the operator.")
public record CreateTopicWithAdminKeyRequest(
        @Schema(
                description = "The private key to use as the admin key for the topic.",
                example = "302e020100300506032b657004220420aabbcc...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String adminPrivateKey,

        @Schema(
                description = "Optional memo to attach to the topic.",
                example = "Enterprise audit log topic",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String memo
) {}
