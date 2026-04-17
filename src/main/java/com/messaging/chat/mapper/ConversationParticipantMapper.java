package com.messaging.chat.mapper;

import com.messaging.chat.dao.entity.Conversation;
import com.messaging.chat.dao.entity.ConversationParticipant;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversationParticipantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "conversation", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ConversationParticipant toEntity(Long userId, @Context Conversation conversation);

    List<Long> toUserIds(List<ConversationParticipant> participants);

    default Long toUserId(ConversationParticipant participant) {
        return participant.getUserId();
    }

    @AfterMapping
    default void setConversation(@MappingTarget ConversationParticipant participant,
                                 @Context Conversation conversation) {
        participant.setConversation(conversation);
    }
}
