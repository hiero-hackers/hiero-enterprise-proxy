package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hiero.base.data.AccountInfo;

@Schema(description = "Detailed account information retrieved from the Hiero mirror node.")
public record AccountInfoResponse(
        @Schema(description = "The Hedera account ID.", example = "0.0.12345")
        String accountId,

        @Schema(description = "The EVM-compatible address of the account.", example = "0x00000000000000000000000000000000000030d9")
        String evmAddress,

        @Schema(description = "The current account balance in tinybars (1 HBAR = 100,000,000 tinybars).", example = "1000000000")
        long balanceTinybars,

        @Schema(description = "The Ethereum nonce of the account, used for EVM-compatible transactions.", example = "0")
        long ethereumNonce,

        @Schema(description = "The pending staking reward in tinybars that has not yet been collected.", example = "5000000")
        long pendingRewardTinybars
) {
    /** Maps a domain {@link AccountInfo} to its API representation. */
    public static AccountInfoResponse from(AccountInfo info) {
        return new AccountInfoResponse(
                info.accountId().toString(),
                info.evmAddress(),
                info.balance(),
                info.ethereumNonce(),
                info.pendingReward()
        );
    }
}
