package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hiero.base.data.Topic;

@Schema(description = "Topic information retrieved from the Hiero mirror node.")
public record TopicResponse(
        @Schema(description = "The topic ID.", example = "0.0.55001")
        String topicId,

        @Schema(description = "The memo attached to the topic.", example = "Enterprise event bus topic")
        String memo,

        @Schema(description = "Whether the topic has been deleted.", example = "false")
        boolean deleted,

        @Schema(description = "The timestamp when the topic was created.", example = "2024-01-15T10:30:00Z")
        String createdTimestamp,

        @Schema(description = "The auto-renew period of the topic in seconds.", example = "7776000")
        int autoRenewPeriod,

        @Schema(description = "Whether the topic has an admin key configured.", example = "true")
        boolean hasAdminKey,

        @Schema(description = "Whether the topic has a submit key configured (private topic).", example = "false")
        boolean hasSubmitKey
) {
    /** Maps a domain {@link Topic} to its API representation. */
    public static TopicResponse from(Topic topic) {
        return new TopicResponse(
                topic.topicId().toString(),
                topic.memo(),
                topic.deleted(),
                topic.createdTimestamp().toString(),
                topic.autoRenewPeriod(),
                topic.adminKey() != null,
                topic.submitKey() != null
        );
    }
}
