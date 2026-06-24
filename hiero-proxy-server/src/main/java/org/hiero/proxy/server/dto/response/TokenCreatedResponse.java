package org.hiero.proxy.server.dto.response;

import com.hedera.hashgraph.sdk.TokenId;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned when a fungible token is created successfully.")
public record TokenCreatedResponse(
        @Schema(description = "The ID of the newly created token.", example = "0.0.55001")
        String tokenId
) {
    /** Maps a SDK {@link TokenId} to the API response. */
    public static TokenCreatedResponse of(TokenId tokenId) {
        return new TokenCreatedResponse(tokenId.toString());
    }
}
