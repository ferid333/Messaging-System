package com.messaging.chat.mapper;

import com.messaging.chat.dao.entity.Conversation;
import com.messaging.chat.model.dto.response.ConversationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ConversationParticipantMapper.class)
public interface ConversationMapper {

    @Mapping(target = "participantUserIds", source = "conversationParticipants")
    ConversationResponse toResponse(Conversation conversation);
}
