package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for rotating a topic key (admin or submit). "
        + "The caller proves ownership with the current admin key; "
        + "the server generates a new key pair and returns it in the response.")
public record UpdateTopicKeyRequest(
        @Schema(
                description = "The current admin private key of the topic, used to authorise the key rotation.",
                example = "302e020100300506032b657004220420aabbcc...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String adminPrivateKey
) {}
