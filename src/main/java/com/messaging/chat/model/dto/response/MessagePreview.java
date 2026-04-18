package com.messaging.chat.model.dto.response;

import com.messaging.chat.model.constant.MessageType;

import java.time.LocalDateTime;

public record MessagePreview(
        Long id,
        MessageType type,
        Long userId,
        String preview,
        LocalDateTime createdAt
) {
}