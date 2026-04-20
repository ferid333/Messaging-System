package com.messaging.chat.model.dto.request;

import com.messaging.chat.model.constant.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request body for sending a message.")
public record SendMessageRequest(
        @Schema(description = "Frontend-generated idempotency key for this message.", example = "550e8400-e29b-41d4-a716-446655440000")
        String clientMessageId,
        @Schema(description = "Message type.", example = "TEXT")
        @NotNull
        MessageType type,
        @Schema(description = "Text content for text messages.", example = "Hello")
        String textContent,
        @Schema(description = "Uploaded attachment ids to link to this message.", example = "[10, 11]")
        List<Long> attachmentIds
) {
}
