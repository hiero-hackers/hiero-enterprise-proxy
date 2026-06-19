package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Private keys required to authorise a delete-and-transfer operation. "
        + "Both the account being deleted and the recipient must sign the transaction.")
public record DeleteAccountToRecipientRequest(
        @Schema(
                description = "The private key of the account being deleted, used to authorise the deletion.",
                example = "302e020100300506032b657004220420aabbcc...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String accountPrivateKey,

        @Schema(
                description = "The private key of the recipient account. Required because the recipient must co-sign the deletion transaction.",
                example = "302e020100300506032b657004220420ddeeff...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String recipientPrivateKey
) {}
