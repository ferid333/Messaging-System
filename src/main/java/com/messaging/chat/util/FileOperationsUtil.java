package com.messaging.chat.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class FileOperationsUtil {


    public String buildStorageKey(Long userId, String fileName) {
        String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return "attachments/%d/%s-%s".formatted(userId, UUID.randomUUID(), sanitizedFileName);
    }

    public String getFileExtension(String fileName) {
        int extensionSeparatorIndex = fileName.lastIndexOf('.');
        if (extensionSeparatorIndex == -1 || extensionSeparatorIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(extensionSeparatorIndex + 1).toLowerCase();
    }
}
