package com.messaging.chat.dao.repository;

import com.messaging.chat.dao.entity.ReadDeliveryState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReadDeliveryStateRepository extends JpaRepository<ReadDeliveryState, Long> {

    Optional<ReadDeliveryState> findByConversationIdAndUserId(Long conversationId, Long userId);
}
