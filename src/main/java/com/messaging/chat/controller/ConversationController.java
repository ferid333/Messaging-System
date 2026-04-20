package com.messaging.chat.controller;


import com.messaging.chat.model.dto.request.CreateConversationRequest;
import com.messaging.chat.model.dto.request.MessageStateRequest;
import com.messaging.chat.model.dto.request.SendMessageRequest;
import com.messaging.chat.model.dto.response.ConversationListResponse;
import com.messaging.chat.model.dto.response.ConversationResponse;
import com.messaging.chat.model.dto.response.MessageResponse;
import com.messaging.chat.model.dto.response.MessageStateResponse;
import com.messaging.chat.service.ConversationService;
import com.messaging.chat.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@Tag(name = "Conversations", description = "Create conversations and fetch conversation lists.")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    @PostMapping
    @Operation(
            summary = "Create conversation",
            description = "Creates a conversation including the current user and the requested participants.",
            parameters = @Parameter(
                    name = USER_ID_HEADER,
                    description = "Current user id",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "1"
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversation created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public ConversationResponse createConversations(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody CreateConversationRequest createConversationRequest) {

        return conversationService.createConversations(userId, createConversationRequest);
    }

    @GetMapping
    @Operation(
            summary = "List conversations",
            description = "Returns conversations for the current user with cursor pagination.",
            parameters = {
                    @Parameter(
                            name = USER_ID_HEADER,
                            description = "Current user id",
                            required = true,
                            in = ParameterIn.HEADER,
                            example = "1"
                    ),
                    @Parameter(
                            name = "limit",
                            description = "Maximum number of conversations to return",
                            example = "20"
                    ),
                    @Parameter(
                            name = "cursor",
                            description = "Last seen conversation id for pagination",
                            example = "50"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "Conversation list returned")
    })
    public List<ConversationListResponse> getConversationList(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) Integer cursor
    ) {

        return conversationService.getConversationList(userId, limit, cursor);
    }

    @GetMapping(path = "/{conversationId}/messages")
    @Operation(
            summary = "List conversation messages",
            description = "Returns the latest messages or messages before a given message id, ordered oldest to newest.",
            parameters = {
                    @Parameter(
                            name = USER_ID_HEADER,
                            description = "Current user id",
                            required = true,
                            in = ParameterIn.HEADER,
                            example = "1"
                    ),
                    @Parameter(
                            name = "conversationId",
                            description = "Conversation id",
                            required = true,
                            example = "20"
                    ),
                    @Parameter(
                            name = "beforeMessageId",
                            description = "Fetch messages older than this message id",
                            example = "171"
                    ),
                    @Parameter(
                            name = "limit",
                            description = "Maximum number of messages to return",
                            example = "30"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Messages returned"),
            @ApiResponse(responseCode = "403", description = "User is not a conversation participant")
    })
    public List<MessageResponse> getMessages(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long beforeMessageId,
            @RequestParam(defaultValue = "30") Integer limit
    ) {
        return messageService.getMessages(userId, conversationId, beforeMessageId, limit);
    }

    @PostMapping(path = "/{conversationId}/messages")
    @Operation(
            summary = "Send message",
            description = "Creates a message in a conversation and links uploaded attachments if provided.",
            parameters = {
                    @Parameter(
                            name = USER_ID_HEADER,
                            description = "Current user id",
                            required = true,
                            in = ParameterIn.HEADER,
                            example = "1"
                    ),
                    @Parameter(
                            name = "conversationId",
                            description = "Conversation id",
                            required = true,
                            example = "20"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or attachment state"),
            @ApiResponse(responseCode = "403", description = "User is not a conversation participant"),
            @ApiResponse(responseCode = "404", description = "Conversation or attachment not found")
    })
    public MessageResponse sendMessage(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest sendMessageRequest
    ) {
        return messageService.sendMessage(userId, conversationId, sendMessageRequest);
    }

    @PostMapping(path = "/{conversationId}/delivery")
    @Operation(
            summary = "Mark messages delivered",
            description = "Updates the user's last delivered message for the conversation.",
            parameters = {
                    @Parameter(
                            name = USER_ID_HEADER,
                            description = "Current user id",
                            required = true,
                            in = ParameterIn.HEADER,
                            example = "1"
                    ),
                    @Parameter(
                            name = "conversationId",
                            description = "Conversation id",
                            required = true,
                            example = "20"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delivery state updated"),
            @ApiResponse(responseCode = "403", description = "User is not a conversation participant"),
            @ApiResponse(responseCode = "404", description = "Conversation or message not found")
    })
    public MessageStateResponse markDelivered(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long conversationId,
            @Valid @RequestBody MessageStateRequest messageStateRequest
    ) {
        return messageService.markDelivered(userId, conversationId, messageStateRequest.messageId());
    }

    @PostMapping(path = "/{conversationId}/read")
    @Operation(
            summary = "Mark messages read",
            description = "Updates the user's last read message for the conversation.",
            parameters = {
                    @Parameter(
                            name = USER_ID_HEADER,
                            description = "Current user id",
                            required = true,
                            in = ParameterIn.HEADER,
                            example = "1"
                    ),
                    @Parameter(
                            name = "conversationId",
                            description = "Conversation id",
                            required = true,
                            example = "20"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Read state updated"),
            @ApiResponse(responseCode = "403", description = "User is not a conversation participant"),
            @ApiResponse(responseCode = "404", description = "Conversation or message not found")
    })
    public MessageStateResponse markRead(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long conversationId,
            @Valid @RequestBody MessageStateRequest messageStateRequest
    ) {
        return messageService.markRead(userId, conversationId, messageStateRequest.messageId());
    }
}
