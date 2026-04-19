package com.messaging.chat.model.dto.response;

import com.messaging.chat.model.constant.FileStatus;

public record AttachmentConfirmResponse(
        Long id,
        String storageKey,
        FileStatus status
) {
}
