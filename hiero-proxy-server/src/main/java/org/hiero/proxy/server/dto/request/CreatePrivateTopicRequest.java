package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating a private topic. "
        + "The server generates a fresh ED25519 submit key and returns it in the response. "
        + "The operator account is used as admin key.")
public record CreatePrivateTopicRequest(
        @Schema(
                description = "Optional memo to attach to the topic.",
                example = "Private enterprise channel",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String memo
) {}
