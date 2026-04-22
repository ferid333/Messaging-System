package com.messaging.chat.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response returned after creating a conversation.")
public record ConversationResponse(
        @Schema(description = "Conversation id.", example = "20")
        Long id,
        @Schema(description = "User ids participating in the conversation.", example = "[1, 2, 3]")
        List<Long> participantUserIds
) {
}
