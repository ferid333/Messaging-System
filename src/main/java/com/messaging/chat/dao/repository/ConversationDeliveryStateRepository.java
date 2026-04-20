package com.messaging.chat.dao.repository;

import com.messaging.chat.dao.entity.ConversationDeliveryState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationDeliveryStateRepository extends JpaRepository<ConversationDeliveryState, Long> {

    Optional<ConversationDeliveryState> findByConversationIdAndUserId(Long conversationId, Long userId);
}
