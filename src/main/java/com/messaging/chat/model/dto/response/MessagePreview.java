package com.messaging.chat.model.dto.response;

import com.messaging.chat.model.constant.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Preview of the latest message in a conversation.")
public record MessagePreview(
        @Schema(description = "Message id.", example = "100")
        Long id,
        @Schema(description = "Message type.", example = "TEXT")
        MessageType type,
        @Schema(description = "Sender user id.", example = "1")
        Long userId,
        @Schema(description = "Short preview content.", example = "Hello")
        String preview,
        @Schema(description = "Message creation time.", example = "2026-04-19T10:05:00")
        LocalDateTime createdAt
) {
}
