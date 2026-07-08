package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Response returned after multiple NFTs are minted in a single transaction.")
public record NftMintedBatchResponse(
        @Schema(description = "The serial numbers assigned to the newly minted NFTs, in the same order as the supplied metadata list.",
                example = "[1, 2, 3]")
        List<Long> serialNumbers
) {
    public static NftMintedBatchResponse of(List<Long> serialNumbers) {
        return new NftMintedBatchResponse(serialNumbers);
    }
}
