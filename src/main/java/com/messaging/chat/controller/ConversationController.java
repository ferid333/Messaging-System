package com.messaging.chat.controller;


import com.messaging.chat.model.dto.request.CreateConversationRequest;
import com.messaging.chat.model.dto.response.ConversationListResponse;
import com.messaging.chat.model.dto.response.ConversationResponse;
import com.messaging.chat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ms-chat/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ConversationResponse createConversations(
            @RequestHeader("userId") Long userId,
            @RequestBody CreateConversationRequest createConversationRequest) {

        return conversationService.createConversations(userId, createConversationRequest);
    }

    @GetMapping
    public List<ConversationListResponse> getConversationList(
            @RequestHeader("userId") Long userId,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) Integer cursor
    ) {

        return conversationService.getConversationList(userId, limit, cursor);
    }
}
