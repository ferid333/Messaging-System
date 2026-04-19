package com.messaging.chat.model.dto.response;

import java.time.Instant;
import java.util.Map;

public record PresignUploadResponse(
        Long attachmentId,
        String storageKey,
        String uploadUrl,
        String httpMethod,
        Instant expiresAt,
        Map<String, String> uploadHeaders
) {
}
