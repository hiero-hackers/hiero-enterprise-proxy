package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Request body for associating or dissociating an account with multiple fungible tokens at once.")
public record TokenBatchAssociateRequest(
        @Schema(
                description = "The list of Hedera token IDs.",
                example = "[\"0.0.55001\", \"0.0.55002\"]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> tokenIds,

        @Schema(
                description = "The Hedera account ID.",
                example = "0.0.12345",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String accountId,

        @Schema(
                description = "The private key of the account to authorise the operation.",
                example = "302e020100300506032b657004220420aabbcc...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String accountKey
) {}
