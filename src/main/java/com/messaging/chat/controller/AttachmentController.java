package com.messaging.chat.controller;

import com.messaging.chat.model.dto.request.PresignUploadRequest;
import com.messaging.chat.model.dto.response.AttachmentConfirmResponse;
import com.messaging.chat.model.dto.response.PresignUploadResponse;
import com.messaging.chat.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Attachments", description = "Create presigned upload URLs and confirm uploaded files.")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(path = "/presign-upload")
    @Operation(
            summary = "Create presigned upload URL",
            description = "Validates file metadata, saves a pending attachment record, and returns an S3 PUT URL.",
            parameters = @Parameter(
                    name = USER_ID_HEADER,
                    description = "Current user id",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "1"
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presigned upload URL created"),
            @ApiResponse(responseCode = "400", description = "Invalid file metadata"),
            @ApiResponse(responseCode = "500", description = "Upload URL could not be created")
    })
    public PresignUploadResponse presignUpload(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody PresignUploadRequest presignUploadRequest
    ) {
        return attachmentService.presignUpload(userId, presignUploadRequest);
    }

    @PostMapping(path = "/{attachmentId}/confirm")
    @Operation(
            summary = "Confirm uploaded attachment",
            description = "Checks S3 to confirm the object was uploaded, then changes attachment status to UPLOADED.",
            parameters = {
                    @Parameter(
                            name = USER_ID_HEADER,
                            description = "Current user id",
                            required = true,
                            in = ParameterIn.HEADER,
                            example = "1"
                    ),
                    @Parameter(
                            name = "attachmentId",
                            description = "Attachment id returned by presign-upload",
                            required = true,
                            example = "10"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attachment confirmed"),
            @ApiResponse(responseCode = "400", description = "File was not uploaded or metadata does not match"),
            @ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    public AttachmentConfirmResponse confirmUpload(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long attachmentId
    ) {
        return attachmentService.confirmUpload(userId, attachmentId);
    }
}
