package org.hiero.proxy.server.exception;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
        @Schema(description = "Error type URI", example = "about:blank") String type,
        @Schema(description = "Short error title", example = "Internal Server Error") String title,
        @Schema(description = "HTTP status code", example = "500") int status,
        @Schema(description = "Detailed error message", example = "An unexpected error occurred") String detail,
        @Schema(description = "Request URI", example = "/api/v1/accounts") String instance
) {}
