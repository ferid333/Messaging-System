package com.messaging.chat.mapper

import com.messaging.chat.dao.entity.Conversation
import org.mapstruct.factory.Mappers
import spock.lang.Specification

class ConversationParticipantMapperSpec extends Specification {

    private ConversationParticipantMapper mapper = Mappers.getMapper(ConversationParticipantMapper)

    def "maps user id and conversation to participant entity"() {
        given:
        def conversation = new Conversation(id: 20L)

        when:
        def participant = mapper.toEntity(1L, conversation)

        then:
        participant.id == null
        participant.userId == 1L
        participant.conversation.is(conversation)
        participant.createdAt == null
        participant.updatedAt == null
    }
}
