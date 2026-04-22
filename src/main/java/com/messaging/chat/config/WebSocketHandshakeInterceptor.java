package com.messaging.chat.config;

import com.messaging.chat.service.WebSocketTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_ID_ATTRIBUTE = "userId";
    private static final String TICKET_PARAM = "ticket";

    private final WebSocketTicketService webSocketTicketService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String ticket = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst(TICKET_PARAM);

        if (!StringUtils.hasText(ticket)) {
            return false;
        }

        Optional<Long> userId = webSocketTicketService.resolveUserId(ticket);
        userId.ifPresent(id -> attributes.put(USER_ID_ATTRIBUTE, id.toString()));
        return userId.isPresent();
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
    }
}
