package com.messaging.chat.mapper

import com.messaging.chat.dao.entity.Attachment
import com.messaging.chat.dao.entity.Conversation
import com.messaging.chat.dao.entity.Message
import com.messaging.chat.model.constant.FileStatus
import com.messaging.chat.model.constant.MessageType
import com.messaging.chat.model.dto.request.SendMessageRequest
import com.messaging.chat.model.dto.response.AttachmentResponse
import com.messaging.chat.model.dto.response.MessageResponse
import org.mapstruct.factory.Mappers
import spock.lang.Specification

import java.time.LocalDateTime

class MessageMapperSpec extends Specification {

    private MessageMapper mapper = Mappers.getMapper(MessageMapper)

    def "maps send message request to message entity"() {
        given:
        def conversation = new Conversation(id: 20L)
        def request = new SendMessageRequest("client-1", MessageType.TEXT, "hello", [])

        when:
        def message = mapper.toEntity(1L, conversation, request)

        then:
        message.id == null
        message.conversation.is(conversation)
        message.senderId == 1L
        message.clientMessageId == "client-1"
        message.type == MessageType.TEXT
        message.textContent == "hello"
        message.attachments == []
    }

    def "maps message to response and preview"() {
        given:
        def createdAt = LocalDateTime.of(2026, 4, 19, 10, 5)
        def message = new Message(
                id: 100L,
                conversation: new Conversation(id: 20L),
                senderId: 1L,
                clientMessageId: "client-1",
                type: MessageType.TEXT,
                textContent: "hello",
                createdAt: createdAt
        )

        when:
        def response = mapper.toResponse(message)
        def preview = mapper.toMessagePreview(message)

        then:
        response.id() == 100L
        response.conversationId() == 20L
        response.senderId() == 1L
        response.clientMessageId() == "client-1"
        response.type() == MessageType.TEXT
        response.textContent() == "hello"
        response.createdAt() == createdAt

        and:
        preview.id() == 100L
        preview.type() == MessageType.TEXT
        preview.userId() == 1L
        preview.preview() == "hello"
        preview.createdAt() == createdAt
    }

    def "maps attachment response and download url"() {
        given:
        def attachment = new Attachment(
                id: 10L,
                originalFileName: "photo.png",
                contentType: "image/png",
                fileSize: 1024L,
                status: FileStatus.UPLOADED
        )

        when:
        def attachmentResponse = mapper.toAttachmentResponse(attachment)
        def withDownload = mapper.toAttachmentResponseWithDownloadUrl(attachmentResponse, "https://download")

        then:
        attachmentResponse.id() == 10L
        attachmentResponse.fileName() == "photo.png"
        attachmentResponse.downloadUrl() == null

        and:
        withDownload.downloadUrl() == "https://download"
        withDownload.fileName() == "photo.png"
    }

    def "maps response with replacement attachments and notification event"() {
        given:
        def createdAt = LocalDateTime.of(2026, 4, 19, 10, 5)
        def response = new MessageResponse(100L, 20L, 1L, "client-1", MessageType.TEXT, "hello", [], createdAt)
        def attachments = [new AttachmentResponse(10L, "photo.png", "image/png", 1024L, FileStatus.UPLOADED, "url")]
        def message = new Message(
                id: 100L,
                conversation: new Conversation(id: 20L),
                senderId: 1L,
                type: MessageType.TEXT,
                textContent: "hello"
        )

        expect:
        mapper.toResponseWithAttachments(response, attachments).attachments() == attachments

        and:
        with(mapper.toMessageNotificationEvent(message, 2L)) {
            recipientUserId() == 2L
            senderId() == 1L
            conversationId() == 20L
            messageId() == 100L
            messageType() == MessageType.TEXT
            textContent() == "hello"
        }
    }
}
