package com.messaging.chat.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationListResponse(
        Long id,
        List<Long> participantUserIds,
        MessagePreview messagePreview,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
