package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for submitting a binary message to a topic. "
        + "The message must be Base64-encoded. "
        + "For private topics, provide the submit key to authorise the submission.")
public record SubmitBinaryMessageRequest(
        @Schema(
                description = "Base64-encoded binary message content to submit to the topic.",
                example = "SGVsbG8sIEhpZXJvIG5ldHdvcmsh",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String messageBase64,

        @Schema(
                description = "The submit private key — only required for private topics.",
                example = "302e020100300506032b657004220420ddeeff...",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String submitKey
) {}
