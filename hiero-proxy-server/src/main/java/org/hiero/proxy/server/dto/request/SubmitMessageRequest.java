package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for submitting a message to a topic. "
        + "For private topics, provide the submit key to authorise the submission.")
public record SubmitMessageRequest(
        @Schema(
                description = "The text message to submit to the topic.",
                example = "Hello, Hiero network!",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String message,

        @Schema(
                description = "The submit private key — only required for private topics.",
                example = "302e020100300506032b657004220420ddeeff...",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String submitKey
) {}
