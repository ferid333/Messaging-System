package com.messaging.chat.dao.repository;

import com.messaging.chat.dao.entity.Conversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
            select distinct c
            from Conversation c
            join c.conversationParticipants cp
            where cp.userId = :userId
            order by c.id desc
            """)
    List<Conversation> findConversationListByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            select distinct c
            from Conversation c
            join c.conversationParticipants cp
            where cp.userId = :userId
              and c.id < :cursor
            order by c.id desc
            """)
    List<Conversation> findConversationListByUserIdAndCursor(@Param("userId") Long userId,
                                                             @Param("cursor") Long cursor,
                                                             Pageable pageable);
}