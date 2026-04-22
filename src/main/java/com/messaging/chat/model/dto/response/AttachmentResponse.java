package com.messaging.chat.model.dto.response;

import com.messaging.chat.model.constant.FileStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Attachment metadata linked to a message.")
public record AttachmentResponse(
        @Schema(description = "Attachment id.", example = "10")
        Long id,
        @Schema(description = "Original file name.", example = "photo.png")
        String fileName,
        @Schema(description = "File MIME type.", example = "image/png")
        String contentType,
        @Schema(description = "File size in bytes.", example = "1048576")
        Long fileSize,
        @Schema(description = "Current upload status.", example = "UPLOADED")
        FileStatus status,
        @Schema(description = "Temporary URL for viewing or downloading the attachment.")
        String downloadUrl
) {
}
