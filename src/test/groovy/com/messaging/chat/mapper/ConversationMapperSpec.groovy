package com.messaging.chat.mapper

import com.messaging.chat.dao.entity.Conversation
import com.messaging.chat.dao.entity.ConversationParticipant
import com.messaging.chat.model.constant.MessageType
import com.messaging.chat.model.dto.response.MessagePreview
import org.mapstruct.factory.Mappers
import spock.lang.Specification

import java.time.LocalDateTime

class ConversationMapperSpec extends Specification {

    private ConversationMapper mapper = Mappers.getMapper(ConversationMapper)

    def "maps conversation participants to response user ids"() {
        given:
        def conversation = conversation(20L, [1L, 2L])

        when:
        def response = mapper.toResponse(conversation)

        then:
        response.id() == 20L
        response.participantUserIds() == [1L, 2L]
    }

    def "maps conversation list response with message preview and timestamps"() {
        given:
        def createdAt = LocalDateTime.of(2026, 4, 19, 10, 0)
        def updatedAt = LocalDateTime.of(2026, 4, 19, 10, 5)
        def conversation = conversation(20L, [1L, 2L])
        conversation.createdAt = createdAt
        conversation.updatedAt = updatedAt
        def preview = new MessagePreview(100L, MessageType.TEXT, 1L, "hello", updatedAt)

        when:
        def response = mapper.toListResponse(conversation, preview)

        then:
        response.id() == 20L
        response.participantUserIds() == [1L, 2L]
        response.messagePreview() == preview
        response.createdAt() == createdAt
        response.updatedAt() == updatedAt
    }

    private static Conversation conversation(Long id, List<Long> userIds) {
        def conversation = new Conversation(id: id)
        conversation.conversationParticipants = userIds.collect {
            new ConversationParticipant(userId: it, conversation: conversation)
        }
        conversation
    }
}
