package com.messaging.chat.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PresignUploadRequest(
        @NotBlank
        String fileName,
        @NotBlank
        String contentType,
        @NotNull
        @Positive
        Long fileSize,
        String checksum
) {
}