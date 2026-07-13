package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hiero.base.data.ContractCallResult;

@Schema(description = "Response containing the result of a smart contract function call.")
public record ContractCallResultResponse(
        @Schema(description = "The gas used by the contract call.", example = "12345")
        long gasUsed,

        @Schema(description = "The cost of the contract call in tinybars.", example = "100000")
        long costInTinybars
) {
    public static ContractCallResultResponse from(ContractCallResult result) {
        return new ContractCallResultResponse(
                result.gasUsed(),
                result.cost().toTinybars()
        );
    }
}
