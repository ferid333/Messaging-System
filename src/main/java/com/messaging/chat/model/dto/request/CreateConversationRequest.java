package com.messaging.chat.model.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateConversationRequest(

        @NotEmpty
        List<Long> participantUserIds)
{
}
