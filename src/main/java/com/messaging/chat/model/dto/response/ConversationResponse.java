package com.messaging.chat.model.dto.response;

import java.util.List;

public record ConversationResponse(Long id, List<Long> participantUserIds) {
}
