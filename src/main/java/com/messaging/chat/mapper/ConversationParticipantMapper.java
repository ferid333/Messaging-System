package com.messaging.chat.mapper;

import com.messaging.chat.dao.entity.Conversation;
import com.messaging.chat.dao.entity.ConversationParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationParticipantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "conversation", source = "conversation")
    @Mapping(target = "updatedAt", ignore = true)
    ConversationParticipant toEntity(Long userId, Conversation conversation);
}
