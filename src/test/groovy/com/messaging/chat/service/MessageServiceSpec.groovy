package com.messaging.chat.service

import com.messaging.chat.dao.entity.Attachment
import com.messaging.chat.dao.entity.Conversation
import com.messaging.chat.dao.entity.ConversationDeliveryState
import com.messaging.chat.dao.entity.ConversationParticipant
import com.messaging.chat.dao.entity.Message
import com.messaging.chat.dao.entity.ReadDeliveryState
import com.messaging.chat.dao.repository.AttachmentRepository
import com.messaging.chat.dao.repository.ConversationDeliveryStateRepository
import com.messaging.chat.dao.repository.ConversationRepository
import com.messaging.chat.dao.repository.MessageRepository
import com.messaging.chat.dao.repository.ReadDeliveryStateRepository
import com.messaging.chat.mapper.MessageMapper
import com.messaging.chat.mapper.MessageStateMapper
import com.messaging.chat.model.constant.FileStatus
import com.messaging.chat.model.constant.MessageType
import com.messaging.chat.model.dto.request.SendMessageRequest
import com.messaging.chat.model.exceptions.FileValidationException
import com.messaging.chat.model.exceptions.ResourceNotFound
import org.mapstruct.factory.Mappers
import org.springframework.data.domain.PageRequest
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

import java.time.LocalDateTime

class MessageServiceSpec extends Specification {

    private ConversationRepository conversationRepository
    private MessageRepository messageRepository
    private AttachmentRepository attachmentRepository
    private ConversationDeliveryStateRepository deliveryStateRepository
    private ReadDeliveryStateRepository readStateRepository
    private AttachmentService attachmentService
    private MessageNotificationPublisher notificationPublisher
    private MessageService service

    def setup() {
        conversationRepository = Mock()
        messageRepository = Mock()
        attachmentRepository = Mock()
        deliveryStateRepository = Mock()
        readStateRepository = Mock()
        attachmentService = Mock()
        notificationPublisher = Mock()
        service = new MessageService(
                conversationRepository,
                messageRepository,
                attachmentRepository,
                deliveryStateRepository,
                readStateRepository,
                Mappers.getMapper(MessageMapper),
                Mappers.getMapper(MessageStateMapper),
                attachmentService,
                notificationPublisher
        )
    }

    def "get messages validates participant and adds download urls for uploaded attachments"() {
        given:
        def message = message(100L, conversation([1L, 2L]), 1L)
        message.attachments = [
                attachment(10L, FileStatus.UPLOADED, null),
                attachment(11L, FileStatus.PENDING, null)
        ]

        when:
        def response = service.getMessages(1L, 20L, null, 30)

        then:
        1 * conversationRepository.existsByIdAndConversationParticipantsUserId(20L, 1L) >> true
        1 * messageRepository.findByConversationIdOrderByIdDesc(20L, PageRequest.of(0, 30)) >> [message]
        1 * attachmentService.createDownloadUrl("attachments/10") >> "https://download/10"
        0 * _

        and:
        response[0].id() == 100L
        response[0].attachments()[0].downloadUrl() == "https://download/10"
        response[0].attachments()[1].downloadUrl() == null
    }

    def "get messages uses before message cursor query"() {
        when:
        def response = service.getMessages(1L, 20L, 90L, 10)

        then:
        1 * conversationRepository.existsByIdAndConversationParticipantsUserId(20L, 1L) >> true
        1 * messageRepository.findByConversationIdAndIdLessThanOrderByIdDesc(20L, 90L, PageRequest.of(0, 10)) >> []
        0 * _

        and:
        response == []
    }

    def "get messages rejects non participant"() {
        when:
        service.getMessages(1L, 20L, null, 30)

        then:
        1 * conversationRepository.existsByIdAndConversationParticipantsUserId(20L, 1L) >> false
        0 * _
        thrown(ResponseStatusException)
    }

    def "send message returns existing message for duplicate client id"() {
        given:
        def conversation = conversation([1L, 2L])
        def existing = message(100L, conversation, 1L)
        existing.clientMessageId = "client-1"

        when:
        def response = service.sendMessage(1L, 20L,
                new SendMessageRequest("client-1", MessageType.TEXT, "ignored", []))

        then:
        1 * conversationRepository.findById(20L) >> Optional.of(conversation)
        1 * conversationRepository.existsByIdAndConversationParticipantsUserId(20L, 1L) >> true
        1 * messageRepository.findByConversationIdAndSenderIdAndClientMessageId(20L, 1L, "client-1") >> Optional.of(existing)
        0 * messageRepository.save(_)
        0 * notificationPublisher._

        and:
        response.id() == 100L
        response.textContent() == "hello"
    }

    def "send message saves message links attachments and publishes notification to other participants"() {
        given:
        def conversation = conversation([1L, 2L, 3L])
        def uploadedAttachment = attachment(10L, FileStatus.UPLOADED, null)

        when:
        def response = service.sendMessage(1L, 20L,
                new SendMessageRequest("client-1", MessageType.TEXT, "hello", [10L]))

        then:
        1 * conversationRepository.findById(20L) >> Optional.of(conversation)
        1 * conversationRepository.existsByIdAndConversationParticipantsUserId(20L, 1L) >> true
        1 * messageRepository.findByConversationIdAndSenderIdAndClientMessageId(20L, 1L, "client-1") >> Optional.empty()
        1 * attachmentRepository.findAllByIdIn({ it as List == [10L] }) >> [uploadedAttachment]
        1 * messageRepository.save({
            it.senderId == 1L &&
                    it.conversation.is(conversation) &&
                    it.attachments == [uploadedAttachment] &&
                    uploadedAttachment.message.is(it)
        } as Message) >> { Message saved ->
            saved.id = 100L
            saved.createdAt = LocalDateTime.of(2026, 4, 19, 10, 5)
            saved
        }
        1 * notificationPublisher.publish({ it.recipientUserId() == 2L && it.messageId() == 100L })
        1 * notificationPublisher.publish({ it.recipientUserId() == 3L && it.messageId() == 100L })
        1 * attachmentService.createDownloadUrl("attachments/10") >> "https://download/10"
        0 * _

        and:
        response.id() == 100L
        response.attachments()[0].downloadUrl() == "https://download/10"
    }

    def "send message rejects invalid attachment state"() {
        given:
        def conversation = conversation([1L, 2L])

        when:
        service.sendMessage(1L, 20L, new SendMessageRequest("client-1", MessageType.TEXT, "hello", [10L]))

        then:
        1 * conversationRepository.findById(20L) >> Optional.of(conversation)
        1 * conversationRepository.existsByIdAndConversationParticipantsUserId(20L, 1L) >> true
        1 * messageRepository.findByConversationIdAndSenderIdAndClientMessageId(20L, 1L, "client-1") >> Optional.empty()
        1 * attachmentRepository.findAllByIdIn(_) >> [attachment(10L, FileStatus.PENDING, null)]
        0 * messageRepository.save(_)
        thrown(FileValidationException)
    }

    def "send message throws when not all attachments exist"() {
        given:
        def conversation = conversation([1L, 2L])

        when:
        service.sendMessage(1L, 20L, new SendMessageRequest("client-1", MessageType.TEXT, "hello", [10L, 11L]))

        then:
        1 * conversationRepository.findById(20L) >> Optional.of(conversation)
        1 * conversationRepository.existsByIdAndConversationParticipantsUserId(20L, 1L) >> true
        1 * messageRepository.findByConversationIdAndSenderIdAndClientMessageId(20L, 1L, "client-1") >> Optional.empty()
        1 * attachmentRepository.findAllByIdIn(_) >> [attachment(10L, FileStatus.UPLOADED, null)]
        0 * messageRepository.save(_)
        thrown(ResourceNotFound)
    }

    def "mark delivered creates state and moves it forward"() {
        given:
        def conversation = conversation([1L, 2L])
        def message = message(100L, conversation, 2L)

        when:
        def response = service.markDelivered(1L, 20L, 100L)

        then:
        1 * conversationRepository.findById(20L) >> Optional.of(conversation)
        1 * conversationRepository.existsByIdAndConversationParticipantsUserId(20L, 1L) >> true
        1 * messageRepository.existsByIdAndConversationId(100L, 20L) >> true
        1 * messageRepository.getReferenceById(100L) >> message
        1 * deliveryStateRepository.findByConversationIdAndUserId(20L, 1L) >> Optional.empty()
        1 * deliveryStateRepository.save({ it.userId == 1L && it.lastMessageId.is(message) }) >> { ConversationDeliveryState state -> state }
        0 * _

        and:
        response == new com.messaging.chat.model.dto.response.MessageStateResponse(20L, 1L, 100L)
    }

    def "mark read does not move state backward"() {
        given:
        def conversation = conversation([1L, 2L])
        def currentMessage = message(120L, conversation, 2L)
        def existingState = new ReadDeliveryState(userId: 1L, conversation: conversation, lastMessageId: currentMessage)

        when:
        def response = service.markRead(1L, 20L, 100L)

        then:
        1 * conversationRepository.findById(20L) >> Optional.of(conversation)
        1 * conversationRepository.existsByIdAndConversationParticipantsUserId(20L, 1L) >> true
        1 * messageRepository.existsByIdAndConversationId(100L, 20L) >> true
        1 * messageRepository.getReferenceById(100L) >> message(100L, conversation, 2L)
        1 * readStateRepository.findByConversationIdAndUserId(20L, 1L) >> Optional.of(existingState)
        0 * readStateRepository.save(_)
        0 * _

        and:
        response == new com.messaging.chat.model.dto.response.MessageStateResponse(20L, 1L, 120L)
    }

    private static Conversation conversation(List<Long> participantIds) {
        def conversation = new Conversation(id: 20L)
        conversation.conversationParticipants = participantIds.collect {
            new ConversationParticipant(userId: it, conversation: conversation)
        }
        conversation
    }

    private static Message message(Long id, Conversation conversation, Long senderId) {
        new Message(
                id: id,
                conversation: conversation,
                senderId: senderId,
                clientMessageId: "client-${id}",
                type: MessageType.TEXT,
                textContent: "hello",
                createdAt: LocalDateTime.of(2026, 4, 19, 10, 5),
                attachments: []
        )
    }

    private static Attachment attachment(Long id, FileStatus status, Message linkedMessage) {
        new Attachment(
                id: id,
                storageKey: "attachments/${id}",
                originalFileName: "file-${id}.png",
                contentType: "image/png",
                fileSize: 1024L,
                status: status,
                message: linkedMessage
        )
    }
}
