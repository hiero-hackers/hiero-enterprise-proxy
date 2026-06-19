package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for deleting an account. The remaining balance will be transferred to the operator account.")
public record DeleteAccountRequest(
        @Schema(
                description = "The private key of the account to be deleted, used to authorise the deletion.",
                example = "302e020100300506032b657004220420aabbcc...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String privateKey
) {}
