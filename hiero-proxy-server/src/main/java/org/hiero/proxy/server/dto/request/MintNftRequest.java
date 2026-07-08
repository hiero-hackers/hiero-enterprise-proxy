package org.hiero.proxy.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for minting a single NFT instance. "
        + "Metadata is provided as a Base64-encoded string.")
public record MintNftRequest(
        @Schema(
                description = "Base64-encoded metadata for the NFT instance (e.g. a URI or JSON blob).",
                example = "aXBmczovL1FtWHh4eHh4eHh4eHh4eHh4eHh4eA==",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String metadata,

        @Schema(
                description = "Private key of the supply account. "
                        + "Required only when the NFT type was created with a custom supply key; "
                        + "omit when the operator is the supply account.",
                example = "302e020100300506032b657004220420aabbcc...",
                nullable = true)
        String supplyKey
) {}
