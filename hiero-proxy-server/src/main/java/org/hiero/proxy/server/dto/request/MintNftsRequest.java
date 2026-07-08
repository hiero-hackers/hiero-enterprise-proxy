package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Request body for minting multiple NFT instances in a single transaction. "
        + "Each metadata entry produces one new NFT serial number. "
        + "Metadata values are Base64-encoded strings.")
public record MintNftsRequest(
        @Schema(
                description = "List of Base64-encoded metadata strings, one per NFT to mint. "
                        + "Each entry produces one NFT with a unique serial number.",
                example = "[\"aXBmczovL1FtQUFBQUFBQUFBQUFBQUE=\", \"aXBmczovL1FtQkJCQkJCQkJCQkJCQkI=\"]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> metadataList,

        @Schema(
                description = "Private key of the supply account. "
                        + "Required only when the NFT type was created with a custom supply key; "
                        + "omit when the operator is the supply account.",
                example = "302e020100300506032b657004220420aabbcc...",
                nullable = true)
        String supplyKey
) {}
