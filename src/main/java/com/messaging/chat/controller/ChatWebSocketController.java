package com.messaging.chat.controller;

import com.messaging.chat.dao.repository.ConversationRepository;
import com.messaging.chat.model.dto.request.MessageStateRequest;
import com.messaging.chat.model.dto.request.SendMessageRequest;
import com.messaging.chat.model.dto.response.MessageResponse;
import com.messaging.chat.model.dto.response.MessageStateResponse;
import com.messaging.chat.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private static final String MESSAGES_QUEUE = "/queue/messages";
    private static final String DELIVERY_QUEUE = "/queue/delivery";
    private static final String READ_QUEUE = "/queue/read";

    private final MessageService messageService;
    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/conversations/{conversationId}/messages")
    public void sendMessage(
            Principal principal,
            @DestinationVariable Long conversationId,
            @Valid @Payload SendMessageRequest sendMessageRequest
    ) {
        Long userId = getUserId(principal);
        MessageResponse messageResponse = messageService.sendMessage(userId, conversationId, sendMessageRequest);

        sendToConversationParticipants(conversationId, MESSAGES_QUEUE, messageResponse);
    }

    @MessageMapping("/conversations/{conversationId}/delivery")
    public void markDelivered(
            Principal principal,
            @DestinationVariable Long conversationId,
            @Valid @Payload MessageStateRequest messageStateRequest
    ) {
        Long userId = getUserId(principal);
        MessageStateResponse messageStateResponse = messageService.markDelivered(
                userId,
                conversationId,
                messageStateRequest.messageId()
        );

        sendToConversationParticipants(conversationId, DELIVERY_QUEUE, messageStateResponse);
    }

    @MessageMapping("/conversations/{conversationId}/read")
    public void markRead(
            Principal principal,
            @DestinationVariable Long conversationId,
            @Valid @Payload MessageStateRequest messageStateRequest
    ) {
        Long userId = getUserId(principal);
        MessageStateResponse messageStateResponse = messageService.markRead(
                userId,
                conversationId,
                messageStateRequest.messageId()
        );

        sendToConversationParticipants(conversationId, READ_QUEUE, messageStateResponse);
    }

    private void sendToConversationParticipants(Long conversationId, String destination, Object payload) {
        List<Long> participantUserIds = conversationRepository.findParticipantUserIdsByConversationId(conversationId);

        participantUserIds.forEach(participantUserId -> messagingTemplate.convertAndSendToUser(
                participantUserId.toString(),
                destination,
                payload
        ));
    }

    private Long getUserId(Principal principal) {
        return Long.valueOf(principal.getName());
    }
}
