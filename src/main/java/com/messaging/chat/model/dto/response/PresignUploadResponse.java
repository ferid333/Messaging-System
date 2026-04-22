package com.messaging.chat.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Response containing the S3 presigned URL and required upload headers.")
public record PresignUploadResponse(
        @Schema(description = "Attachment id saved with PENDING status.", example = "10")
        Long attachmentId,
        @Schema(description = "S3 object key.", example = "attachments/1/uuid-photo.png")
        String storageKey,
        @Schema(description = "Temporary S3 URL used by the client to upload the file.")
        String uploadUrl,
        @Schema(description = "HTTP method to use against uploadUrl.", example = "PUT")
        String httpMethod,
        @Schema(description = "Expiration time for the presigned upload URL.", example = "2026-04-19T02:17:36Z")
        Instant expiresAt,
        @Schema(description = "Headers that must be sent when uploading the file to S3.")
        Map<String, String> uploadHeaders
) {
}
