package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hiero.base.data.TokenInfo;

@Schema(description = "Detailed information about a fungible token retrieved from the mirror node.")
public record TokenInfoResponse(
        @Schema(description = "The token ID.", example = "0.0.55001")
        String tokenId,

        @Schema(description = "The full name of the token.", example = "My Enterprise Token")
        String name,

        @Schema(description = "The ticker symbol of the token.", example = "MET")
        String symbol,

        @Schema(description = "The token type (FUNGIBLE_COMMON or NON_FUNGIBLE_UNIQUE).", example = "FUNGIBLE_COMMON")
        String type,

        @Schema(description = "The memo attached to the token.", example = "Enterprise reward token", nullable = true)
        String memo,

        @Schema(description = "Number of decimal places.", example = "0")
        long decimals,

        @Schema(description = "The supply type (INFINITE or FINITE).", example = "INFINITE")
        String supplyType,

        @Schema(description = "Current total supply of the token.", example = "10000")
        String totalSupply,

        @Schema(description = "Maximum supply of the token.", example = "0")
        String maxSupply,

        @Schema(description = "The treasury account that holds newly minted tokens.", example = "0.0.1001")
        String treasuryAccountId,

        @Schema(description = "Whether the token has been deleted.", example = "false")
        boolean deleted
) {
    /** Maps a domain {@link TokenInfo} to the API representation. */
    public static TokenInfoResponse from(TokenInfo info) {
        return new TokenInfoResponse(
                info.tokenId().toString(),
                info.name(),
                info.symbol(),
                info.type().toString(),
                info.memo(),
                info.decimals(),
                info.supplyType().toString(),
                info.totalSupply(),
                info.maxSupply(),
                info.treasuryAccountId().toString(),
                info.deleted()
        );
    }
}
