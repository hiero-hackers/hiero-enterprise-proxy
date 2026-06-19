package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hiero.base.data.Account;

public record AccountResponse(
        @Schema(description = "The newly generated Hedera Account ID", example = "0.0.12345")
        String accountId,

        @Schema(description = "The public key associated with the account", example = "302a300506032b6570032100...")
        String publicKey,

        @Schema(description = "The private key to control the account (keep this secure!)", example = "302e020100300506032b657004220420...")
        String privateKey
) {
    /** Maps a domain {@link Account} to its API representation. */
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.accountId().toString(),
                account.publicKey().toString(),
                account.privateKey().toString()
        );
    }
}
