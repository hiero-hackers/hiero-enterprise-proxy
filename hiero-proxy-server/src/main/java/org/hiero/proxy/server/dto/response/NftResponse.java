package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Base64;
import org.hiero.base.data.Nft;

@Schema(description = "Represents a single minted NFT instance retrieved from the mirror node.")
public record NftResponse(
        @Schema(description = "The NFT token type ID.", example = "0.0.66001")
        String tokenId,

        @Schema(description = "The serial number of this NFT instance.", example = "1")
        long serialNumber,

        @Schema(description = "The account ID that currently owns this NFT. Null if the NFT has been burned.",
                example = "0.0.12345",
                nullable = true)
        String ownerId,

        @Schema(description = "Base64-encoded metadata associated with this NFT instance.",
                example = "aXBmczovL1FtWHh4eHh4eHh4eHh4eHh4eHh4eA==")
        String metadata
) {
    /** Maps a domain {@link Nft} to the API representation. */
    public static NftResponse from(Nft nft) {
        return new NftResponse(
                nft.tokenId().toString(),
                nft.serial(),
                nft.owner() != null ? nft.owner().toString() : null,
                Base64.getEncoder().encodeToString(nft.metadata())
        );
    }
}
