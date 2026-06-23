package org.hiero.proxy.server.dto.response;

import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TopicId;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned when a topic's admin key, submit key, and memo are updated atomically. "
        + "Both new key pairs were generated server-side — save them securely.")
public record TopicUpdatedResponse(
        @Schema(description = "The topic ID that was updated.", example = "0.0.55001")
        String topicId,

        @Schema(description = "The newly generated admin private key. Keep this secure.",
                example = "302e020100300506032b657004220420aabbcc...")
        String newAdminPrivateKey,

        @Schema(description = "The newly generated admin public key.",
                example = "302a300506032b6570032100aabbcc...")
        String newAdminPublicKey,

        @Schema(description = "The newly generated submit private key. Keep this secure.",
                example = "302e020100300506032b657004220420ddeeff...")
        String newSubmitPrivateKey,

        @Schema(description = "The newly generated submit public key.",
                example = "302a300506032b6570032100ddeeff...")
        String newSubmitPublicKey,

        @Schema(description = "The updated memo.", example = "Updated enterprise event bus")
        String memo
) {
    /** Creates a response for an atomic topic update with freshly generated key pairs. */
    public static TopicUpdatedResponse of(
            TopicId topicId,
            PrivateKey newAdminKey,
            PrivateKey newSubmitKey,
            String memo) {
        return new TopicUpdatedResponse(
                topicId.toString(),
                newAdminKey.toString(),
                newAdminKey.getPublicKey().toString(),
                newSubmitKey.toString(),
                newSubmitKey.getPublicKey().toString(),
                memo
        );
    }
}
