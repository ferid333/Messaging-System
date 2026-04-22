package com.messaging.chat.service;

import com.messaging.chat.dao.entity.Attachment;
import com.messaging.chat.dao.entity.Conversation;
import com.messaging.chat.dao.entity.ConversationDeliveryState;
import com.messaging.chat.dao.entity.ConversationParticipant;
import com.messaging.chat.dao.entity.Message;
import com.messaging.chat.dao.entity.ReadDeliveryState;
import com.messaging.chat.dao.repository.AttachmentRepository;
import com.messaging.chat.dao.repository.ConversationDeliveryStateRepository;
import com.messaging.chat.dao.repository.ConversationRepository;
import com.messaging.chat.dao.repository.MessageRepository;
import com.messaging.chat.dao.repository.ReadDeliveryStateRepository;
import com.messaging.chat.logging.DPLogger;
import com.messaging.chat.mapper.MessageMapper;
import com.messaging.chat.mapper.MessageStateMapper;
import com.messaging.chat.model.constant.FileStatus;
import com.messaging.chat.model.dto.event.MessageNotificationEvent;
import com.messaging.chat.model.dto.request.SendMessageRequest;
import com.messaging.chat.model.dto.response.AttachmentResponse;
import com.messaging.chat.model.dto.response.MessageResponse;
import com.messaging.chat.model.dto.response.MessageStateResponse;
import com.messaging.chat.model.exceptions.FileValidationException;
import com.messaging.chat.model.exceptions.ResourceNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final DPLogger logger = DPLogger.getLogger(MessageService.class);

    private static final int ZERO = 0;

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AttachmentRepository attachmentRepository;
    private final ConversationDeliveryStateRepository conversationDeliveryStateRepository;
    private final ReadDeliveryStateRepository readDeliveryStateRepository;
    private final MessageMapper messageMapper;
    private final MessageStateMapper messageStateMapper;
    private final AttachmentService attachmentService;
    private final MessageNotificationPublisher messageNotificationPublisher;

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(Long userId, Long conversationId, Long beforeMessageId, Integer limit) {
        logger.info("Action.log.start getMessages userId: {}, conversationId: {}, beforeMessageId: {}",
                userId, conversationId, beforeMessageId);

        validateConversationParticipant(userId, conversationId);

        PageRequest pageRequest = PageRequest.of(ZERO, limit);
        List<Message> messages = beforeMessageId == null
                ? messageRepository.findByConversationIdOrderByIdDesc(conversationId, pageRequest)
                : messageRepository.findByConversationIdAndIdLessThanOrderByIdDesc(
                        conversationId,
                        beforeMessageId,
                        pageRequest
                );

        List<MessageResponse> messageResponses = messages.stream()
                .map(this::toResponseWithDownloadUrls)
                .toList();

        logger.info("Action.log.end getMessages userId: {}, conversationId: {}, count: {}",
                userId, conversationId, messageResponses.size());
        return messageResponses;
    }

    @Transactional
    public MessageResponse sendMessage(Long userId, Long conversationId, SendMessageRequest sendMessageRequest) {
        logger.info("Action.log.start sendMessage userId: {}, conversationId: {}", userId, conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFound("Conversation not found"));

        validateConversationParticipant(userId, conversationId);

        Message existingMessage = findExistingMessage(userId, conversationId, sendMessageRequest.clientMessageId());
        if (existingMessage != null) {
            logger.info("Action.log.end sendMessage duplicate userId: {}, conversationId: {}, messageId: {}",
                    userId, conversationId, existingMessage.getId());
            return toResponseWithDownloadUrls(existingMessage);
        }

        Message message = messageMapper.toEntity(userId, conversation, sendMessageRequest);
        List<Attachment> attachments = getValidAttachments(sendMessageRequest.attachmentIds());
        attachments.forEach(attachment -> attachment.setMessage(message));
        message.setAttachments(attachments);

        Message savedMessage = messageRepository.save(message);
        publishMessageNotificationEvents(savedMessage);

        logger.info("Action.log.end sendMessage userId: {}, conversationId: {}, messageId: {}",
                userId, conversationId, savedMessage.getId());
        return toResponseWithDownloadUrls(savedMessage);
    }

    @Transactional
    public MessageStateResponse markDelivered(Long userId, Long conversationId, Long messageId) {
        logger.info("Action.log.start markDelivered userId: {}, conversationId: {}, messageId: {}",
                userId, conversationId, messageId);

        Conversation conversation = validateStateUpdate(userId, conversationId, messageId);
        Message message = messageRepository.getReferenceById(messageId);
        ConversationDeliveryState state = conversationDeliveryStateRepository.findByConversationIdAndUserId(
                conversationId, userId)
                .orElseGet(() -> messageStateMapper.toDeliveryState(userId, conversation));

        if (shouldMoveForward(state.getLastMessageId(), messageId)) {
            state.setLastMessageId(message);
            state = conversationDeliveryStateRepository.save(state);
        }

        Long storedMessageId = state.getLastMessageId().getId();
        logger.info("Action.log.end markDelivered userId: {}, conversationId: {}, messageId: {}",
                userId, conversationId, storedMessageId);
        return new MessageStateResponse(conversationId, userId, storedMessageId);
    }

    @Transactional
    public MessageStateResponse markRead(Long userId, Long conversationId, Long messageId) {
        logger.info("Action.log.start markRead userId: {}, conversationId: {}, messageId: {}",
                userId, conversationId, messageId);

        Conversation conversation = validateStateUpdate(userId, conversationId, messageId);
        Message message = messageRepository.getReferenceById(messageId);
        ReadDeliveryState state = readDeliveryStateRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseGet(() -> messageStateMapper.toReadState(userId, conversation));

        if (shouldMoveForward(state.getLastMessageId(), messageId)) {
            state.setLastMessageId(message);
            state = readDeliveryStateRepository.save(state);
        }

        Long storedMessageId = state.getLastMessageId().getId();
        logger.info("Action.log.end markRead userId: {}, conversationId: {}, messageId: {}",
                userId, conversationId, storedMessageId);
        return new MessageStateResponse(conversationId, userId, storedMessageId);
    }

    private Conversation validateStateUpdate(Long userId, Long conversationId, Long messageId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFound("Conversation not found"));

        if (!conversationRepository.existsByIdAndConversationParticipantsUserId(conversationId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not participant of conversation");
        }

        if (!messageRepository.existsByIdAndConversationId(messageId, conversationId)) {
            throw new ResourceNotFound("Message not found in conversation");
        }

        return conversation;
    }

    private void validateConversationParticipant(Long userId, Long conversationId) {
        if (!conversationRepository.existsByIdAndConversationParticipantsUserId(conversationId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not participant of conversation");
        }
    }

    private boolean shouldMoveForward(Message currentMessage, Long newMessageId) {
        return currentMessage == null || currentMessage.getId() < newMessageId;
    }

    private Message findExistingMessage(Long userId, Long conversationId, String clientMessageId) {
        if (clientMessageId == null || clientMessageId.isBlank()) {
            return null;
        }

        return messageRepository.findByConversationIdAndSenderIdAndClientMessageId(
                conversationId,
                userId,
                clientMessageId
        ).orElse(null);
    }

    private MessageResponse toResponseWithDownloadUrls(Message message) {
        MessageResponse messageResponse = messageMapper.toResponse(message);
        List<AttachmentResponse> attachments = message.getAttachments().stream()
                .map(this::toAttachmentResponseWithDownloadUrl)
                .toList();

        return messageMapper.toResponseWithAttachments(messageResponse, attachments);
    }

    private void publishMessageNotificationEvents(Message message) {
        List<MessageNotificationEvent> events = message.getConversation().getConversationParticipants().stream()
                .map(ConversationParticipant::getUserId)
                .filter(participantUserId -> !participantUserId.equals(message.getSenderId()))
                .map(recipientUserId -> messageMapper.toMessageNotificationEvent(message, recipientUserId))
                .toList();

        if (events.isEmpty()) {
            return;
        }

        events.forEach(messageNotificationPublisher::publish);
    }

    private AttachmentResponse toAttachmentResponseWithDownloadUrl(Attachment attachment) {
        AttachmentResponse attachmentResponse = messageMapper.toAttachmentResponse(attachment);
        String downloadUrl = attachment.getStatus() == FileStatus.UPLOADED
                ? attachmentService.createDownloadUrl(attachment.getStorageKey())
                : null;

        return messageMapper.toAttachmentResponseWithDownloadUrl(attachmentResponse, downloadUrl);
    }

    private List<Attachment> getValidAttachments(List<Long> attachmentIds) {
        if (CollectionUtils.isEmpty(attachmentIds)) {
            return Collections.emptyList();
        }

        Set<Long> uniqueAttachmentIds = new LinkedHashSet<>(attachmentIds);
        List<Attachment> attachments = attachmentRepository.findAllByIdIn(uniqueAttachmentIds);
        if (attachments.size() != uniqueAttachmentIds.size()) {
            throw new ResourceNotFound("One or more attachments were not found");
        }

        attachments.forEach(this::validateAttachment);
        return attachments;
    }

    private void validateAttachment(Attachment attachment) {
        if (attachment.getMessage() != null) {
            throw new FileValidationException("Attachment is already linked to a message");
        }

        if (attachment.getStatus() != FileStatus.UPLOADED) {
            throw new FileValidationException("Attachment must be uploaded before sending message");
        }
    }
}
