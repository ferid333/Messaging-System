package com.messaging.chat.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request body for creating a conversation.")
public record CreateConversationRequest(

        @Schema(
                description = "Participant user ids to add to the conversation. The current user is added automatically.",
                example = "[2, 3]"
        )
        @NotEmpty
        List<Long> participantUserIds)
{
}
