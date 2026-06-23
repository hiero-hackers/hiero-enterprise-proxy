package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for atomically updating a topic's admin key, submit key, and memo in a single transaction. "
        + "The server generates fresh ED25519 key pairs for both the admin and submit keys and returns them in the response.")
public record UpdateTopicRequest(
        @Schema(
                description = "The current admin private key of the topic, used to authorise the update.",
                example = "302e020100300506032b657004220420aabbcc...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String currentAdminPrivateKey,

        @Schema(
                description = "The new memo to set on the topic.",
                example = "Updated enterprise event bus",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String memo
) {}
