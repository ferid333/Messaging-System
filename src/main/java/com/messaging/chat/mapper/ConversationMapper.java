package com.messaging.chat.mapper;

import com.messaging.chat.dao.entity.Conversation;
import com.messaging.chat.dao.entity.ConversationParticipant;
import com.messaging.chat.model.dto.response.ConversationListResponse;
import com.messaging.chat.model.dto.response.ConversationResponse;
import com.messaging.chat.model.dto.response.MessagePreview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(target = "participantUserIds", source = "conversationParticipants",
            qualifiedByName = "participantsToUserIds")
    ConversationResponse toResponse(Conversation conversation);

    @Mapping(target = "participantUserIds", source = "conversation.conversationParticipants",
            qualifiedByName = "participantsToUserIds")
    @Mapping(target = "id", source = "conversation.id")
    @Mapping(target = "messagePreview", source = "messagePreview")
    @Mapping(target = "createdAt", source = "conversation.createdAt")
    @Mapping(target = "updatedAt", source = "conversation.updatedAt")
    ConversationListResponse toListResponse(Conversation conversation, MessagePreview messagePreview);

    @Named("participantsToUserIds")
    default List<Long> mapParticipantsToUserIds(List<ConversationParticipant> participants) {
        return participants.stream()
                .map(ConversationParticipant::getUserId)
                .toList();
    }
}