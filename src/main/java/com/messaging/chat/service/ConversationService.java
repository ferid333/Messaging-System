package com.messaging.chat.service;

import com.messaging.chat.dao.entity.Conversation;
import com.messaging.chat.dao.entity.ConversationParticipant;
import com.messaging.chat.dao.repository.ConversationRepository;
import com.messaging.chat.model.dto.ConversationResponse;
import com.messaging.chat.model.dto.CreateConversationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
}
