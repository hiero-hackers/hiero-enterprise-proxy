package org.hiero.proxy.server.dto.response;

import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TopicId;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned when a topic key (admin or submit) has been rotated. "
        + "The new key pair was generated server-side — save it securely.")
public record TopicKeyRotationResponse(
        @Schema(description = "The topic ID whose key was rotated.", example = "0.0.55001")
        String topicId,

        @Schema(description = "The newly generated private key. Keep this secure.",
                example = "302e020100300506032b657004220420ddeeff...")
        String newPrivateKey,

        @Schema(description = "The newly generated public key.",
                example = "302a300506032b6570032100ddeeff...")
        String newPublicKey
) {
    /** Creates a response for a key rotation. */
    public static TopicKeyRotationResponse of(TopicId topicId, PrivateKey newKey) {
        return new TopicKeyRotationResponse(
                topicId.toString(),
                newKey.toString(),
                newKey.getPublicKey().toString()
        );
    }
}
