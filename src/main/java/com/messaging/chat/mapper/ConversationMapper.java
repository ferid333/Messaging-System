package com.messaging.chat.mapper;

import com.messaging.chat.dao.entity.Conversation;
import com.messaging.chat.dao.entity.ConversationParticipant;
import com.messaging.chat.model.dto.response.ConversationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(target = "participantUserIds", source = "conversationParticipants", qualifiedByName = "participantsToUserIds")
    ConversationResponse toResponse(Conversation conversation);

    @Named("participantsToUserIds")
    default List<Long> mapParticipantsToUserIds(List<ConversationParticipant> participants) {
        return participants.stream()
                .map(ConversationParticipant::getUserId)
                .toList();
    }
}
