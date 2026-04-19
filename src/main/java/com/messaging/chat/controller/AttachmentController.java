package com.messaging.chat.controller;

import com.messaging.chat.model.dto.request.PresignUploadRequest;
import com.messaging.chat.model.dto.response.AttachmentConfirmResponse;
import com.messaging.chat.model.dto.response.PresignUploadResponse;
import com.messaging.chat.service.AttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.messaging.chat.model.constant.Headers.USER_ID_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ms-chat/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(path = "/presign-upload")
    public PresignUploadResponse presignUpload(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody PresignUploadRequest presignUploadRequest
    ) {
        return attachmentService.presignUpload(userId, presignUploadRequest);
    }

    @PostMapping(path = "/{attachmentId}/confirm")
    public AttachmentConfirmResponse confirmUpload(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long attachmentId
    ) {
        return attachmentService.confirmUpload(userId, attachmentId);
    }
}
