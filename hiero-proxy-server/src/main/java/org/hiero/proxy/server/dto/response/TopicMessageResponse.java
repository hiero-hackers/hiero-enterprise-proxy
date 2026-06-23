package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Base64;
import org.hiero.base.data.TopicMessage;

@Schema(description = "A message retrieved from a topic on the Hiero mirror node.")
public record TopicMessageResponse(
        @Schema(description = "The topic ID this message belongs to.", example = "0.0.55001")
        String topicId,

        @Schema(description = "The sequence number of the message within the topic.", example = "42")
        long sequenceNumber,

        @Schema(description = "The message content.", example = "Hello, Hiero network!")
        String message,

        @Schema(description = "The consensus timestamp when the message was submitted.", example = "2024-01-15T10:31:00Z")
        String consensusTimestamp,

        @Schema(description = "The account ID that paid for the transaction.", example = "0.0.12345")
        String payerAccountId,

        @Schema(description = "Base64-encoded running hash of all messages up to and including this one.",
                example = "d2VsbGNvbWUgdG8gaGllcm8=")
        String runningHashBase64,

        @Schema(description = "The version of the running hash algorithm used.", example = "3")
        int runningHashVersion,

        @Schema(description = "Whether this message is part of a chunked message sequence.", example = "false")
        boolean chunked
) {
    /** Maps a domain {@link TopicMessage} to its API representation. */
    public static TopicMessageResponse from(TopicMessage topicMessage) {
        return new TopicMessageResponse(
                topicMessage.topicId().toString(),
                topicMessage.sequenceNumber(),
                topicMessage.message(),
                topicMessage.consensusTimestamp().toString(),
                topicMessage.payerAccountId().toString(),
                Base64.getEncoder().encodeToString(topicMessage.runningHash()),
                topicMessage.runningHashVersion(),
                topicMessage.chunkInfo() != null
        );
    }
}
