package com.messaging.chat.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import com.messaging.chat.model.constant.FileStatus;

@Schema(description = "Response returned after confirming an attachment upload.")
public record AttachmentConfirmResponse(
        @Schema(description = "Attachment id.", example = "10")
        Long id,
        @Schema(description = "S3 object key for the uploaded file.", example = "attachments/1/uuid-photo.png")
        String storageKey,
        @Schema(description = "Current attachment status.", example = "UPLOADED")
        FileStatus status
) {
}
