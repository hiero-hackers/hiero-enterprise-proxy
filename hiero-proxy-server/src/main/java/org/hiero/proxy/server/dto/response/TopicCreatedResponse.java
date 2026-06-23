package org.hiero.proxy.server.dto.response;

import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TopicId;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned when a topic is created. "
        + "For private topics, the generated submit key pair is included — save it securely.")
public record TopicCreatedResponse(
        @Schema(description = "The newly created topic ID.", example = "0.0.55001")
        String topicId,

        @Schema(description = "The generated submit private key — only set for private topics. Keep this secure.",
                example = "302e020100300506032b657004220420ddeeff...",
                nullable = true)
        String submitPrivateKey,

        @Schema(description = "The generated submit public key — only set for private topics.",
                example = "302a300506032b6570032100ddeeff...",
                nullable = true)
        String submitPublicKey
) {
    /** Creates a response for a public topic (no submit key). */
    public static TopicCreatedResponse of(TopicId topicId) {
        return new TopicCreatedResponse(topicId.toString(), null, null);
    }

    /** Creates a response for a private topic, including the generated submit key pair. */
    public static TopicCreatedResponse of(TopicId topicId, PrivateKey submitPrivateKey) {
        return new TopicCreatedResponse(
                topicId.toString(),
                submitPrivateKey.toString(),
                submitPrivateKey.getPublicKey().toString()
        );
    }
}
