package com.messaging.chat.mapper;

import com.messaging.chat.dao.entity.Message;
import com.messaging.chat.model.dto.response.MessagePreview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "userId", source = "senderId")
    @Mapping(target = "preview", source = "textContent")
    MessagePreview toMessagePreview(Message message);
}
