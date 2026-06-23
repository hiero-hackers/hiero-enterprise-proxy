package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating a public topic. The operator account is used as admin key.")
public record CreateTopicRequest(
        @Schema(
                description = "Optional memo to attach to the topic.",
                example = "Enterprise event bus topic",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String memo
) {}
