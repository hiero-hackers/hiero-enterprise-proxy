package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for deleting a topic. The admin key is required to authorise the deletion.")
public record DeleteTopicRequest(
        @Schema(
                description = "The admin private key of the topic, used to authorise the deletion.",
                example = "302e020100300506032b657004220420aabbcc...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String adminPrivateKey
) {}
