package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hiero.base.data.Token;

@Schema(description = "Summary information about a token, typically returned in list queries.")
public record TokenResponse(
        @Schema(description = "The ID of the token.", example = "0.0.55001")
        String tokenId,

        @Schema(description = "The full name of the token.", example = "My Enterprise Token")
        String name,

        @Schema(description = "The ticker symbol of the token.", example = "MET")
        String symbol,

        @Schema(description = "The token type.", example = "FUNGIBLE_COMMON")
        String type,

        @Schema(description = "The number of decimals.", example = "0")
        long decimals
) {
    public static TokenResponse from(Token token) {
        return new TokenResponse(
                token.tokenId() != null ? token.tokenId().toString() : null,
                token.name(),
                token.symbol(),
                token.type().toString(),
                token.decimals()
        );
    }
}
