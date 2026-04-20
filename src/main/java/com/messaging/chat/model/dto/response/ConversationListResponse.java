package com.messaging.chat.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Conversation summary returned in conversation list responses.")
public record ConversationListResponse(
        @Schema(description = "Conversation id.", example = "20")
        Long id,
        @Schema(description = "User ids participating in the conversation.", example = "[1, 2, 3]")
        List<Long> participantUserIds,
        @Schema(description = "Latest message preview for the conversation.")
        MessagePreview messagePreview,
        @Schema(description = "Conversation creation time.", example = "2026-04-19T10:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Conversation last update time.", example = "2026-04-19T10:05:00")
        LocalDateTime updatedAt
) {
}
