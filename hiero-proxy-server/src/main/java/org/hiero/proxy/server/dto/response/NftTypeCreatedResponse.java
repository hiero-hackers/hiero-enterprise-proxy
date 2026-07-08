package org.hiero.proxy.server.dto.response;

import com.hedera.hashgraph.sdk.TokenId;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned when a new NFT token type is created successfully.")
public record NftTypeCreatedResponse(
        @Schema(description = "The ID of the newly created NFT token type.", example = "0.0.66001")
        String tokenId
) {
    /** Maps a SDK {@link TokenId} to the API response. */
    public static NftTypeCreatedResponse of(TokenId tokenId) {
        return new NftTypeCreatedResponse(tokenId.toString());
    }
}
