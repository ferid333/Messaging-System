package com.messaging.chat.controller;


import com.messaging.chat.model.dto.request.CreateConversationRequest;
import com.messaging.chat.model.dto.response.ConversationListResponse;
import com.messaging.chat.model.dto.response.ConversationResponse;
import com.messaging.chat.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.messaging.chat.model.constant.Headers.USER_ID_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ms-chat/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ConversationResponse createConversations(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody CreateConversationRequest createConversationRequest) {

        return conversationService.createConversations(userId, createConversationRequest);
    }

    @GetMapping
    public List<ConversationListResponse> getConversationList(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) Integer cursor
    ) {

        return conversationService.getConversationList(userId, limit, cursor);
    }
}
