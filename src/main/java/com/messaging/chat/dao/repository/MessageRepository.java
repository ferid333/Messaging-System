package com.messaging.chat.dao.repository;

import com.messaging.chat.dao.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    boolean existsByIdAndConversationId(Long messageId, Long conversationId);

    List<Message> findByConversationIdOrderByIdDesc(Long conversationId, Pageable pageable);

    List<Message> findByConversationIdAndIdLessThanOrderByIdDesc(
            Long conversationId,
            Long beforeMessageId,
            Pageable pageable
    );

    Optional<Message> findByConversationIdAndSenderIdAndClientMessageId(
            Long conversationId,
            Long senderId,
            String clientMessageId
    );
}
