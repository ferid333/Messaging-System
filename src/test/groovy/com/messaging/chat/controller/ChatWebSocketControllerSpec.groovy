package com.messaging.chat.controller

import com.messaging.chat.dao.repository.ConversationRepository
import com.messaging.chat.model.constant.MessageType
import com.messaging.chat.model.dto.request.MessageStateRequest
import com.messaging.chat.model.dto.request.SendMessageRequest
import com.messaging.chat.model.dto.response.MessageResponse
import com.messaging.chat.model.dto.response.MessageStateResponse
import com.messaging.chat.service.MessageService
import org.springframework.messaging.simp.SimpMessagingTemplate
import spock.lang.Specification

import java.security.Principal
import java.time.LocalDateTime

class ChatWebSocketControllerSpec extends Specification {

    private MessageService messageService
    private ConversationRepository conversationRepository
    private SimpMessagingTemplate messagingTemplate
    private ChatWebSocketController controller
    private Principal principal

    def setup() {
        messageService = Mock()
        conversationRepository = Mock()
        messagingTemplate = Mock()
        controller = new ChatWebSocketController(messageService, conversationRepository, messagingTemplate)
        principal = Stub(Principal) {
            getName() >> "1"
        }
    }

    def "send message saves message and publishes to all participants private queues"() {
        given:
        def request = new SendMessageRequest("client-1", MessageType.TEXT, "hello", [])
        def response = new MessageResponse(100L, 20L, 1L, "client-1", MessageType.TEXT, "hello", [], LocalDateTime.now())

        when:
        controller.sendMessage(principal, 20L, request)

        then:
        1 * messageService.sendMessage(1L, 20L, request) >> response
        1 * conversationRepository.findParticipantUserIdsByConversationId(20L) >> [1L, 2L]
        1 * messagingTemplate.convertAndSendToUser("1", "/queue/messages", response)
        1 * messagingTemplate.convertAndSendToUser("2", "/queue/messages", response)
        0 * _
    }

    def "mark delivered publishes delivery state to participants"() {
        given:
        def response = new MessageStateResponse(20L, 2L, 100L)

        when:
        controller.markDelivered(principal, 20L, new MessageStateRequest(100L))

        then:
        1 * messageService.markDelivered(1L, 20L, 100L) >> response
        1 * conversationRepository.findParticipantUserIdsByConversationId(20L) >> [1L, 2L]
        1 * messagingTemplate.convertAndSendToUser("1", "/queue/delivery", response)
        1 * messagingTemplate.convertAndSendToUser("2", "/queue/delivery", response)
        0 * _
    }

    def "mark read publishes read state to participants"() {
        given:
        def response = new MessageStateResponse(20L, 2L, 100L)

        when:
        controller.markRead(principal, 20L, new MessageStateRequest(100L))

        then:
        1 * messageService.markRead(1L, 20L, 100L) >> response
        1 * conversationRepository.findParticipantUserIdsByConversationId(20L) >> [1L, 2L]
        1 * messagingTemplate.convertAndSendToUser("1", "/queue/read", response)
        1 * messagingTemplate.convertAndSendToUser("2", "/queue/read", response)
        0 * _
    }
}
