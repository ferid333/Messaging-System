package com.messaging.chat.model.dto.response;

import com.messaging.chat.model.constant.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Message response.")
public record MessageResponse(
        @Schema(description = "Message id.", example = "100")
        Long id,
        @Schema(description = "Conversation id.", example = "20")
        Long conversationId,
        @Schema(description = "Sender user id.", example = "1")
        Long senderId,
        @Schema(description = "Frontend-generated idempotency key.", example = "550e8400-e29b-41d4-a716-446655440000")
        String clientMessageId,
        @Schema(description = "Message type.", example = "TEXT")
        MessageType type,
        @Schema(description = "Text content.", example = "Hello")
        String textContent,
        @Schema(description = "Attachments linked to this message.")
        List<AttachmentResponse> attachments,
        @Schema(description = "Message creation time.", example = "2026-04-19T10:05:00")
        LocalDateTime createdAt
) {
}
