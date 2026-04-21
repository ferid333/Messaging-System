package com.messaging.chat.service

import com.messaging.chat.dao.entity.Conversation
import com.messaging.chat.dao.entity.ConversationParticipant
import com.messaging.chat.dao.entity.Message
import com.messaging.chat.dao.repository.ConversationRepository
import com.messaging.chat.mapper.ConversationMapper
import com.messaging.chat.mapper.ConversationParticipantMapper
import com.messaging.chat.mapper.MessageMapper
import com.messaging.chat.model.constant.MessageType
import com.messaging.chat.model.dto.request.CreateConversationRequest
import org.mapstruct.factory.Mappers
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

import java.time.LocalDateTime

class ConversationServiceSpec extends Specification {

    private ConversationRepository conversationRepository
    private ConversationService service

    def setup() {
        conversationRepository = Mock()
        service = new ConversationService(
                conversationRepository,
                Mappers.getMapper(ConversationMapper),
                Mappers.getMapper(ConversationParticipantMapper),
                Mappers.getMapper(MessageMapper)
        )
    }

    def "create conversation includes current user and deduplicates participant ids"() {
        given:
        def request = new CreateConversationRequest([2L, 1L, null, 2L, 3L])

        when:
        def response = service.createConversations(1L, request)

        then:
        1 * conversationRepository.save({
            it.conversationParticipants*.userId == [1L, 2L, 3L] &&
                    it.conversationParticipants.every { participant -> participant.conversation.is(it) }
        } as Conversation) >> { Conversation conversation ->
            conversation.id = 20L
            conversation
        }
        0 * _

        and:
        response.id() == 20L
        response.participantUserIds() == [1L, 2L, 3L]
    }

    def "get conversation list uses first page when cursor is null and maps latest message preview"() {
        given:
        def conversation = conversationWithMessages(20L)

        when:
        def response = service.getConversationList(1L, 10, null)

        then:
        1 * conversationRepository.findConversationListByUserId(1L, PageRequest.of(0, 10)) >> [conversation]
        0 * _

        and:
        response.size() == 1
        response[0].id() == 20L
        response[0].participantUserIds() == [1L, 2L]
        response[0].messagePreview().id() == 101L
        response[0].messagePreview().preview() == "new"
    }

    def "get conversation list uses cursor query when cursor is positive"() {
        when:
        def response = service.getConversationList(1L, 10, 50)

        then:
        1 * conversationRepository.findConversationListByUserIdAndCursor(1L, 50L, PageRequest.of(0, 10)) >> []
        0 * _

        and:
        response == []
    }

    private static Conversation conversationWithMessages(Long id) {
        def conversation = new Conversation(id: id)
        conversation.conversationParticipants = [
                new ConversationParticipant(userId: 1L, conversation: conversation),
                new ConversationParticipant(userId: 2L, conversation: conversation)
        ]
        conversation.messages = [
                new Message(id: 100L, senderId: 1L, type: MessageType.TEXT, textContent: "old",
                        createdAt: LocalDateTime.of(2026, 4, 19, 10, 0)),
                new Message(id: 101L, senderId: 2L, type: MessageType.TEXT, textContent: "new",
                        createdAt: LocalDateTime.of(2026, 4, 19, 10, 5))
        ]
        conversation
    }
}
