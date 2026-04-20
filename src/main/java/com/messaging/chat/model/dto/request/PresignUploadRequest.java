package com.messaging.chat.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Metadata required to create a presigned S3 upload URL.")
public record PresignUploadRequest(
        @Schema(description = "Original file name.", example = "photo.png")
        @NotBlank
        String fileName,
        @Schema(description = "MIME type sent when uploading to S3.", example = "image/png")
        @NotBlank
        String contentType,
        @Schema(description = "File size in bytes. Must be less than 5 MB.", example = "1048576", minimum = "1")
        @NotNull
        @Positive
        Long fileSize,
        @Schema(description = "Optional base64 Content-MD5 checksum.", example = "1B2M2Y8AsgTpgAmY7PhCfg==")
        String checksum
) {
}
