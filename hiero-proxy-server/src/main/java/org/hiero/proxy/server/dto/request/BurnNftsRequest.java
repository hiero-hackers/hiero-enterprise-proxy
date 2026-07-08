package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Request body for burning one or more NFT serial numbers. "
        + "The supply account must sign the transaction.")
public record BurnNftsRequest(
        @Schema(
                description = "The serial numbers of the NFTs to burn.",
                example = "[1, 2, 3]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Set<Long> serialNumbers,

        @Schema(
                description = "Private key of the supply account. "
                        + "Required only when the NFT type was created with a custom supply key; "
                        + "omit when the operator is the supply account.",
                example = "302e020100300506032b657004220420aabbcc...",
                nullable = true)
        String supplyKey
) {}
