package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for updating a topic's memo. The admin key is required to authorise the update.")
public record UpdateTopicMemoRequest(
        @Schema(
                description = "The current admin private key of the topic, used to authorise the memo update.",
                example = "302e020100300506032b657004220420aabbcc...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String adminPrivateKey,

        @Schema(
                description = "The new memo value to set on the topic.",
                example = "Updated enterprise event bus topic",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String memo
) {}
