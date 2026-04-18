package com.messaging.chat.service;

import com.messaging.chat.dao.entity.Conversation;
import com.messaging.chat.dao.entity.Message;
import com.messaging.chat.dao.repository.ConversationRepository;
import com.messaging.chat.logging.DPLogger;
import com.messaging.chat.mapper.ConversationMapper;
import com.messaging.chat.mapper.ConversationParticipantMapper;
import com.messaging.chat.mapper.MessageMapper;
import com.messaging.chat.model.dto.request.CreateConversationRequest;
import com.messaging.chat.model.dto.response.ConversationListResponse;
import com.messaging.chat.model.dto.response.ConversationResponse;
import com.messaging.chat.model.dto.response.MessagePreview;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private static final DPLogger logger = DPLogger.getLogger(ConversationService.class);

    private static final int ZERO = 0;

    private final ConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper;
    private final ConversationParticipantMapper conversationParticipantMapper;
    private final MessageMapper messageMapper;

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

    @Transactional(readOnly = true)
    public List<ConversationListResponse> getConversationList(Long userId, Integer limit, Integer cursor) {
        logger.info("Action.log.start getConversationList userId: {}, cursor: {}", userId, cursor);

        List<Conversation> conversations = cursor == null || cursor <= ZERO
                ? conversationRepository.findConversationListByUserId(userId, PageRequest.of(ZERO, limit))
                : conversationRepository.findConversationListByUserIdAndCursor(
                userId,
                cursor.longValue(),
                PageRequest.of(ZERO, limit)
        );

        List<ConversationListResponse> conversationList = conversations.stream()
                .map(conversation -> conversationMapper.toListResponse(conversation,
                        buildMessagePreview(conversation)))
                .toList();

        logger.info("Action.log.end getConversationList userId: {} cursor: {}", userId, cursor);
        return conversationList;
    }

    private MessagePreview buildMessagePreview(Conversation conversation) {
        return conversation.getMessages().stream()
                .max(Comparator.comparing(Message::getCreatedAt))
                .map(messageMapper::toMessagePreview)
                .orElse(null);
    }
}
