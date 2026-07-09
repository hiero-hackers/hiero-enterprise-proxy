package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hiero.base.data.NetworkFee;

@Schema(description = "EVM gas fee for a specific Hiero smart contract or Ethereum transaction type.")
public record NetworkFeeResponse(
        @Schema(description = "The gas cost for this transaction type.", example = "853000")
        long gas,

        @Schema(description = "The Hedera transaction type this fee applies to.", example = "CRYPTOTRANSFER")
        String transactionType
) {
    public static NetworkFeeResponse from(NetworkFee fee) {
        return new NetworkFeeResponse(fee.gas(), fee.transactionType());
    }
}
