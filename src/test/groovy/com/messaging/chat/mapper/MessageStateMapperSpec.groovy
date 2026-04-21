package com.messaging.chat.mapper

import com.messaging.chat.dao.entity.Conversation
import org.mapstruct.factory.Mappers
import spock.lang.Specification

class MessageStateMapperSpec extends Specification {

    private MessageStateMapper mapper = Mappers.getMapper(MessageStateMapper)

    def "maps delivery and read state shell entities"() {
        given:
        def conversation = new Conversation(id: 20L)

        when:
        def delivery = mapper.toDeliveryState(1L, conversation)
        def read = mapper.toReadState(2L, conversation)

        then:
        delivery.id == null
        delivery.userId == 1L
        delivery.conversation.is(conversation)
        delivery.lastMessageId == null

        and:
        read.id == null
        read.userId == 2L
        read.conversation.is(conversation)
        read.lastMessageId == null
    }
}
