package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hiero.base.data.StakingRewardTransfer;

@Schema(description = "A staking reward transfer entry within a transaction.")
public record StakingRewardTransferResponse(
        @Schema(description = "The account ID that received the staking reward.", example = "0.0.12345")
        String accountId,

        @Schema(description = "Staking reward amount in tinybar.", example = "5000000")
        long amount
) {
    public static StakingRewardTransferResponse from(StakingRewardTransfer t) {
        return new StakingRewardTransferResponse(t.account().toString(), t.amount());
    }
}
