package com.messaging.chat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.messaging.chat.model.constant.MessageType
import com.messaging.chat.model.dto.request.CreateConversationRequest
import com.messaging.chat.model.dto.request.MessageStateRequest
import com.messaging.chat.model.dto.request.SendMessageRequest
import com.messaging.chat.model.dto.response.ConversationListResponse
import com.messaging.chat.model.dto.response.ConversationResponse
import com.messaging.chat.model.dto.response.MessageResponse
import com.messaging.chat.model.dto.response.MessageStateResponse
import com.messaging.chat.service.ConversationService
import com.messaging.chat.service.MessageService
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import spock.lang.Specification

import java.time.LocalDateTime

import static com.messaging.chat.model.constant.Headers.USER_ID_HEADER
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ConversationControllerSpec extends Specification {

    private ConversationService conversationService
    private MessageService messageService
    private MockMvc mockMvc
    private ObjectMapper objectMapper

    def setup() {
        conversationService = Mock()
        messageService = Mock()
        objectMapper = new ObjectMapper().findAndRegisterModules()

        def validator = new LocalValidatorFactoryBean()
        validator.afterPropertiesSet()

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ConversationController(conversationService, messageService))
                .setValidator(validator)
                .build()
    }

    def "create conversation delegates to service"() {
        given:
        def request = new CreateConversationRequest([2L, 3L])
        def response = new ConversationResponse(11L, [1L, 2L, 3L])

        when:
        def result = mockMvc.perform(post("/ms-chat/conversations")
                .header(USER_ID_HEADER, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then:
        1 * conversationService.createConversations(1L, { it.participantUserIds() == [2L, 3L] }) >> response
        0 * _

        and:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(11))
                .andExpect(jsonPath('$.participantUserIds[0]').value(1))
                .andExpect(jsonPath('$.participantUserIds[2]').value(3))
    }

    def "list conversations passes pagination parameters"() {
        given:
        def response = [new ConversationListResponse(11L, [1L, 2L], null, null, null)]

        when:
        def result = mockMvc.perform(get("/ms-chat/conversations")
                .header(USER_ID_HEADER, "1")
                .param("limit", "15")
                .param("cursor", "50"))

        then:
        1 * conversationService.getConversationList(1L, 15, 50) >> response
        0 * _

        and:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$[0].id').value(11))
                .andExpect(jsonPath('$[0].participantUserIds[1]').value(2))
    }

    def "get messages delegates to message service"() {
        given:
        def response = [
                new MessageResponse(100L, 20L, 1L, "client-1", MessageType.TEXT, "hello", [], LocalDateTime.now())
        ]

        when:
        def result = mockMvc.perform(get("/ms-chat/conversations/20/messages")
                .header(USER_ID_HEADER, "1")
                .param("beforeMessageId", "90")
                .param("limit", "10"))

        then:
        1 * messageService.getMessages(1L, 20L, 90L, 10) >> response
        0 * _

        and:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$[0].id').value(100))
                .andExpect(jsonPath('$[0].textContent').value("hello"))
    }

    def "send message delegates to message service"() {
        given:
        def request = new SendMessageRequest("client-1", MessageType.TEXT, "hello", [])
        def response = new MessageResponse(100L, 20L, 1L, "client-1", MessageType.TEXT, "hello", [], LocalDateTime.now())

        when:
        def result = mockMvc.perform(post("/ms-chat/conversations/20/messages")
                .header(USER_ID_HEADER, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then:
        1 * messageService.sendMessage(1L, 20L, { it.clientMessageId() == "client-1" }) >> response
        0 * _

        and:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(100))
                .andExpect(jsonPath('$.conversationId').value(20))
    }

    def "mark delivered delegates to message service"() {
        given:
        def request = new MessageStateRequest(100L)
        def response = new MessageStateResponse(20L, 1L, 100L)

        when:
        def result = mockMvc.perform(post("/ms-chat/conversations/20/delivery")
                .header(USER_ID_HEADER, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then:
        1 * messageService.markDelivered(1L, 20L, 100L) >> response
        0 * _

        and:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.conversationId').value(20))
                .andExpect(jsonPath('$.messageId').value(100))
    }

    def "mark read delegates to message service"() {
        given:
        def request = new MessageStateRequest(100L)
        def response = new MessageStateResponse(20L, 1L, 100L)

        when:
        def result = mockMvc.perform(post("/ms-chat/conversations/20/read")
                .header(USER_ID_HEADER, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then:
        1 * messageService.markRead(1L, 20L, 100L) >> response
        0 * _

        and:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.conversationId').value(20))
                .andExpect(jsonPath('$.messageId').value(100))
    }

    def "invalid request body returns bad request before service call"() {
        when:
        def result = mockMvc.perform(post("/ms-chat/conversations")
                .header(USER_ID_HEADER, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString([participantUserIds: []])))

        then:
        0 * conversationService._
        0 * messageService._

        and:
        result.andExpect(status().isBadRequest())
    }
}
