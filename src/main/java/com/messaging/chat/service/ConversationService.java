package com.messaging.chat.service;

import com.messaging.chat.dao.entity.Conversation;
import com.messaging.chat.dao.repository.ConversationRepository;
import com.messaging.chat.logging.DPLogger;
import com.messaging.chat.mapper.ConversationMapper;
import com.messaging.chat.mapper.ConversationParticipantMapper;
import com.messaging.chat.model.dto.request.CreateConversationRequest;
import com.messaging.chat.model.dto.response.ConversationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private static final DPLogger logger = DPLogger.getLogger(ConversationService.class);

    private final ConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper;
    private final ConversationParticipantMapper conversationParticipantMapper;

    @Transactional
    public ConversationResponse createConversations(Long userId, CreateConversationRequest createConversationRequest) {
        logger.info("Action.log.start createConversations userId: {}", userId);

        LinkedHashSet<Long> participantIds = new LinkedHashSet<>();
        participantIds.add(userId);
        createConversationRequest.participantUserIds().stream()
                .filter(Objects::nonNull)
                .forEach(participantIds::add);

        Conversation conversation = new Conversation();
        conversation.setConversationParticipants(
                participantIds.stream()
                        .map(participantId -> conversationParticipantMapper.toEntity(participantId, conversation))
                        .toList()
        );

        Conversation savedConversation = conversationRepository.save(conversation);
        logger.info("Action.log.end createConversations userId: {}", userId);

        return conversationMapper.toResponse(savedConversation);
    }
}
