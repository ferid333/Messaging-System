package com.messaging.chat.service;

import com.messaging.chat.logging.DPLogger;
import com.messaging.chat.model.dto.event.MessageNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MessageNotificationPublisher {

    private static final DPLogger logger = DPLogger.getLogger(MessageNotificationPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.notification-exchange}")
    private String notificationExchangeName;

    @Value("${rabbitmq.notification-routing-key}")
    private String notificationRoutingKey;

    public void publish(MessageNotificationEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void sendToQueue(MessageNotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(notificationExchangeName, notificationRoutingKey, event);
        } catch (AmqpException ex) {
            logger.warn("Action.log.failed sendToQueue recipientUserId: {}, conversationId: {}, messageId: {}, error: {}",
                    event.recipientUserId(), event.conversationId(), event.messageId(), ex.getMessage());
        }
    }
}
