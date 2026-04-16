package com.messaging.chat.controller;


import com.messaging.chat.model.dto.ConversationResponse;
import com.messaging.chat.model.dto.CreateConversationRequest;
import com.messaging.chat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ms-chat/conversations")
public class ConversationController {

    private final ConversationService conversationService;

}
