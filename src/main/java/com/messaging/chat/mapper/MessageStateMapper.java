package com.messaging.chat.mapper;

import com.messaging.chat.dao.entity.Conversation;
import com.messaging.chat.dao.entity.ConversationDeliveryState;
import com.messaging.chat.dao.entity.ReadDeliveryState;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageStateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastMessageId", ignore = true)
    @Mapping(target = "conversation", source = "conversation")
    ConversationDeliveryState toDeliveryState(Long userId, Conversation conversation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastMessageId", ignore = true)
    @Mapping(target = "conversation", source = "conversation")
    ReadDeliveryState toReadState(Long userId, Conversation conversation);
}
