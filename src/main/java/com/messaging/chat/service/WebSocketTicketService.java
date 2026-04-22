package com.messaging.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WebSocketTicketService {

    private final StringRedisTemplate redisTemplate;


    private static final String TICKET_KEY_PREFIX = "chat:ws-ticket:";


    public Optional<Long> resolveUserId(String ticket) {
        String userId = redisTemplate.opsForValue().getAndDelete(TICKET_KEY_PREFIX + ticket);
        if (!StringUtils.hasText(userId)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.valueOf(userId));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
