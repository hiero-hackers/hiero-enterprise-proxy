package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after a single NFT is minted successfully.")
public record NftMintedResponse(
        @Schema(description = "The serial number assigned to the newly minted NFT.", example = "1")
        long serialNumber
) {
    public static NftMintedResponse of(long serialNumber) {
        return new NftMintedResponse(serialNumber);
    }
}
