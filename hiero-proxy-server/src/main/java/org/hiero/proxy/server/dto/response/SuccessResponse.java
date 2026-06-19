package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Confirmation response returned when an operation completes successfully but has no data to return.")
public record SuccessResponse(
        @Schema(description = "Human-readable confirmation message.", example = "Account memo updated successfully.")
        String message
) {
    public static SuccessResponse of(String message) {
        return new SuccessResponse(message);
    }
}
