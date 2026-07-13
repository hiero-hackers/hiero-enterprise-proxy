package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned when a hook is stored successfully.")
public record HookStoreResponse(
        @Schema(description = "The ID of the hook that was stored.", example = "0.0.12345")
        String hookId,
        
        @Schema(description = "A success message.", example = "Hook stored successfully.")
        String message
) {
    public static HookStoreResponse of(String hookId) {
        return new HookStoreResponse(hookId, "Hook " + hookId + " stored successfully.");
    }
}
