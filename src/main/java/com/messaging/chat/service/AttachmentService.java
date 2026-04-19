package com.messaging.chat.service;

import com.messaging.chat.dao.entity.Attachment;
import com.messaging.chat.dao.repository.AttachmentRepository;
import com.messaging.chat.logging.DPLogger;
import com.messaging.chat.mapper.AttachmentMapper;
import com.messaging.chat.model.dto.request.PresignUploadRequest;
import com.messaging.chat.model.dto.response.AttachmentConfirmResponse;
import com.messaging.chat.model.dto.response.PresignUploadResponse;
import com.messaging.chat.model.exceptions.FileValidationException;
import com.messaging.chat.model.exceptions.ResourceNotFound;
import com.messaging.chat.util.FileOperationsUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.messaging.chat.model.constant.FileConstants.EXECUTABLE_CONTENT_TYPES;
import static com.messaging.chat.model.constant.FileConstants.EXECUTABLE_FILE_EXTENSIONS;
import static com.messaging.chat.model.constant.FileStatus.FAILED;
import static com.messaging.chat.model.constant.FileStatus.UPLOADED;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final DPLogger logger = DPLogger.getLogger(AttachmentService.class);
    private static final long MAX_UPLOAD_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final int ZERO = 0;

    @Value("${storage.s3.bucket-name}")
    private String bucketName;

    @Value("${storage.s3.presign-duration-minutes}")
    private Long presignDurationMinutes;

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentMapper attachmentMapper;

    @Transactional
    public PresignUploadResponse presignUpload(Long userId, PresignUploadRequest presignUploadRequest) {
        logger.info("Action.log.start presignUpload userId: {}, fileName: {}", userId, presignUploadRequest.fileName());

        validateUploadedFile(presignUploadRequest);

        String storageKey = FileOperationsUtil.buildStorageKey(userId, presignUploadRequest.fileName());
        Attachment attachment = attachmentRepository.save(attachmentMapper.toPendingEntity(
                userId,
                storageKey,
                presignUploadRequest
        ));
        PresignedPutObjectRequest presignedPutObjectRequest = createPresignedPutObjectRequest(
                storageKey,
                presignUploadRequest
        );
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(presignDurationMinutes));

        logger.info("Action.log.end presignUpload userId: {}, storageKey: {}", userId, storageKey);
        return new PresignUploadResponse(
                attachment.getId(),
                storageKey,
                presignedPutObjectRequest.url().toString(),
                "PUT",
                expiresAt,
                extractUploadHeaders(presignedPutObjectRequest)
        );
    }

    @Transactional
    public AttachmentConfirmResponse confirmUpload(Long userId, Long attachmentId) {
        logger.info("Action.log.start confirmUpload userId: {}, attachmentId: {}", userId, attachmentId);

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFound("Attachment not found"));

        HeadObjectResponse headObjectResponse = getUploadedObjectMetadata(attachment.getStorageKey());
        validateUploadedObjectMetadata(attachment, headObjectResponse);

        attachment.setStatus(UPLOADED);
        Attachment savedAttachment = attachmentRepository.save(attachment);

        logger.info("Action.log.end confirmUpload userId: {}, attachmentId: {}", userId, attachmentId);
        return attachmentMapper.toAttachmentConfirmResponse(savedAttachment);
    }

    private void validateUploadedFile(PresignUploadRequest presignUploadRequest) {
        if (presignUploadRequest.fileSize() >= MAX_UPLOAD_SIZE_BYTES) {
            throw new FileValidationException("File size must be less than 5 MB");
        }

        String contentType = presignUploadRequest.contentType().split(";")[ZERO].trim().toLowerCase();
        if (EXECUTABLE_CONTENT_TYPES.contains(contentType)) {
            throw new FileValidationException("Executable files are not allowed");
        }

        String fileExtension = FileOperationsUtil.getFileExtension(presignUploadRequest.fileName());
        if (EXECUTABLE_FILE_EXTENSIONS.contains(fileExtension)) {
            throw new FileValidationException("Executable files are not allowed");
        }
    }

    private PresignedPutObjectRequest createPresignedPutObjectRequest(
            String storageKey,
            PresignUploadRequest presignUploadRequest
    ) {
        PutObjectRequest.Builder putObjectRequestBuilder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(storageKey)
                .contentType(presignUploadRequest.contentType())
                .contentLength(presignUploadRequest.fileSize());

        if (presignUploadRequest.checksum() != null && !presignUploadRequest.checksum().isBlank()) {
            putObjectRequestBuilder.contentMD5(presignUploadRequest.checksum());
        }

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignDurationMinutes))
                .putObjectRequest(putObjectRequestBuilder.build())
                .build();

        return s3Presigner.presignPutObject(presignRequest);
    }

    private HeadObjectResponse getUploadedObjectMetadata(String storageKey) {
        try {
            return s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build());
        } catch (NoSuchKeyException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File has not been uploaded yet", exception);
        } catch (S3Exception exception) {
            if (exception.statusCode() == HttpStatus.NOT_FOUND.value()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File has not been uploaded yet", exception);
            }
            throw exception;
        }
    }

    private void validateUploadedObjectMetadata(Attachment attachment, HeadObjectResponse headObjectResponse) {
        if (!attachment.getFileSize().equals(headObjectResponse.contentLength())) {
            attachment.setStatus(FAILED);
            attachmentRepository.save(attachment);
            throw new FileValidationException("Uploaded file size does not match presigned request");
        }

        if (!attachment.getContentType().equalsIgnoreCase(headObjectResponse.contentType())) {
            attachment.setStatus(FAILED);
            attachmentRepository.save(attachment);
            throw new FileValidationException("Uploaded file content type does not match presigned request");
        }
    }

    private Map<String, String> extractUploadHeaders(PresignedPutObjectRequest presignedPutObjectRequest) {
        Map<String, String> headers = new LinkedHashMap<>();
        presignedPutObjectRequest.httpRequest().headers()
                .forEach((headerName, headerValues) -> headers.put(headerName,
                        String.join(",", headerValues)));
        return headers;
    }
}
