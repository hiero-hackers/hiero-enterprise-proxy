package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hiero.base.data.Contract;

import java.time.Instant;

@Schema(description = "Response containing smart contract information.")
public record ContractResponse(
        @Schema(description = "The ID of the smart contract.", example = "0.0.1234")
        String contractId,

        @Schema(description = "Whether the contract is deleted.", example = "false")
        boolean deleted,

        @Schema(description = "The creation timestamp in seconds.", example = "1700000000")
        Long createdTimestampEpochSecond,

        @Schema(description = "The expiration timestamp in seconds.", example = "1800000000")
        Long expirationTimestampEpochSecond,

        @Schema(description = "The EVM address of the contract.", example = "0x00000000000000000000000000000000000004d2")
        String evmAddress,

        @Schema(description = "The memo associated with the contract.", example = "My Smart Contract")
        String memo
) {
    public static ContractResponse from(Contract contract) {
        return new ContractResponse(
                contract.contractId().toString(),
                contract.deleted(),
                contract.createdTimestamp().getEpochSecond(),
                contract.expirationTimestamp() != null ? contract.expirationTimestamp().getEpochSecond() : null,
                contract.evmAddress(),
                contract.memo()
        );
    }
}
