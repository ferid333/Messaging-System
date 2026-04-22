package com.messaging.chat.mapper;

import com.messaging.chat.dao.entity.Attachment;
import com.messaging.chat.dao.entity.Conversation;
import com.messaging.chat.dao.entity.Message;
import com.messaging.chat.model.dto.event.MessageNotificationEvent;
import com.messaging.chat.model.dto.request.SendMessageRequest;
import com.messaging.chat.model.dto.response.AttachmentResponse;
import com.messaging.chat.model.dto.response.MessagePreview;
import com.messaging.chat.model.dto.response.MessageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "conversation", source = "conversation")
    @Mapping(target = "senderId", source = "userId")
    @Mapping(target = "clientMessageId", source = "sendMessageRequest.clientMessageId")
    @Mapping(target = "type", source = "sendMessageRequest.type")
    @Mapping(target = "textContent", source = "sendMessageRequest.textContent")
    @Mapping(target = "attachments", ignore = true)
    Message toEntity(Long userId, Conversation conversation, SendMessageRequest sendMessageRequest);

    @Mapping(target = "userId", source = "senderId")
    @Mapping(target = "preview", source = "textContent")
    MessagePreview toMessagePreview(Message message);

    @Mapping(target = "conversationId", source = "conversation.id")
    MessageResponse toResponse(Message message);

    @Mapping(target = "fileName", source = "originalFileName")
    @Mapping(target = "downloadUrl", ignore = true)
    AttachmentResponse toAttachmentResponse(Attachment attachment);

    @Mapping(target = "id", source = "messageResponse.id")
    @Mapping(target = "conversationId", source = "messageResponse.conversationId")
    @Mapping(target = "senderId", source = "messageResponse.senderId")
    @Mapping(target = "clientMessageId", source = "messageResponse.clientMessageId")
    @Mapping(target = "type", source = "messageResponse.type")
    @Mapping(target = "textContent", source = "messageResponse.textContent")
    @Mapping(target = "attachments", source = "attachments")
    @Mapping(target = "createdAt", source = "messageResponse.createdAt")
    MessageResponse toResponseWithAttachments(MessageResponse messageResponse, List<AttachmentResponse> attachments);

    @Mapping(target = "id", source = "attachmentResponse.id")
    @Mapping(target = "fileName", source = "attachmentResponse.fileName")
    @Mapping(target = "contentType", source = "attachmentResponse.contentType")
    @Mapping(target = "fileSize", source = "attachmentResponse.fileSize")
    @Mapping(target = "status", source = "attachmentResponse.status")
    @Mapping(target = "downloadUrl", source = "downloadUrl")
    AttachmentResponse toAttachmentResponseWithDownloadUrl(AttachmentResponse attachmentResponse, String downloadUrl);

    @Mapping(target = "recipientUserId", source = "recipientUserId")
    @Mapping(target = "senderId", source = "message.senderId")
    @Mapping(target = "conversationId", source = "message.conversation.id")
    @Mapping(target = "messageId", source = "message.id")
    @Mapping(target = "messageType", source = "message.type")
    @Mapping(target = "textContent", source = "message.textContent")
    MessageNotificationEvent toMessageNotificationEvent(Message message, Long recipientUserId);
}
