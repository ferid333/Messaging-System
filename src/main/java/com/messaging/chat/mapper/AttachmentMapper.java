package com.messaging.chat.mapper;

import com.messaging.chat.dao.entity.Attachment;
import com.messaging.chat.model.constant.FileStatus;
import com.messaging.chat.model.dto.request.PresignUploadRequest;
import com.messaging.chat.model.dto.response.AttachmentConfirmResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = FileStatus.class)
public interface AttachmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "uploaderId", source = "userId")
    @Mapping(target = "storageKey", source = "storageKey")
    @Mapping(target = "originalFileName", source = "presignUploadRequest.fileName")
    @Mapping(target = "contentType", source = "presignUploadRequest.contentType")
    @Mapping(target = "fileSize", source = "presignUploadRequest.fileSize")
    @Mapping(target = "checksum", source = "presignUploadRequest.checksum")
    @Mapping(target = "status", expression = "java(FileStatus.PENDING)")
    Attachment toPendingEntity(Long userId, String storageKey, PresignUploadRequest presignUploadRequest);


    AttachmentConfirmResponse toAttachmentConfirmResponse(Attachment attachment);
}
