package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after a mint or burn operation, containing the updated total supply.")
public record TokenSupplyResponse(
        @Schema(description = "The total supply of the token after the operation.", example = "10000")
        long totalSupply
) {
    public static TokenSupplyResponse of(long totalSupply) {
        return new TokenSupplyResponse(totalSupply);
    }
}
