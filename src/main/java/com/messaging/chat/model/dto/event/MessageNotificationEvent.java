package com.messaging.chat.model.dto.event;

import com.messaging.chat.model.constant.MessageType;

public record MessageNotificationEvent(
        Long recipientUserId,
        Long senderId,
        Long conversationId,
        Long messageId,
        MessageType messageType,
        String textContent
) {
}
