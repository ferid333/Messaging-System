package com.messaging.chat.service

import com.messaging.chat.model.constant.MessageType
import com.messaging.chat.model.dto.event.MessageNotificationEvent
import org.springframework.amqp.AmqpException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class MessageNotificationPublisherSpec extends Specification {

    private ApplicationEventPublisher applicationEventPublisher
    private RabbitTemplate rabbitTemplate
    private MessageNotificationPublisher publisher

    def setup() {
        applicationEventPublisher = Mock()
        rabbitTemplate = Mock()
        publisher = new MessageNotificationPublisher(applicationEventPublisher, rabbitTemplate)
        ReflectionTestUtils.setField(publisher, "notificationExchangeName", "notificationExchange")
        ReflectionTestUtils.setField(publisher, "notificationRoutingKey", "notificationRouting")
    }

    def "publish delegates to application event publisher"() {
        given:
        def event = event()

        when:
        publisher.publish(event)

        then:
        1 * applicationEventPublisher.publishEvent(event)
        0 * _
    }

    def "send to queue publishes rabbit message"() {
        given:
        def event = event()

        when:
        publisher.sendToQueue(event)

        then:
        1 * rabbitTemplate.convertAndSend("notificationExchange", "notificationRouting", event)
        0 * _
    }

    def "send to queue swallows amqp exception"() {
        when:
        publisher.sendToQueue(event())

        then:
        1 * rabbitTemplate.convertAndSend(_, _, _) >> { throw new AmqpException("down") }
        noExceptionThrown()
    }

    private static MessageNotificationEvent event() {
        new MessageNotificationEvent(2L, 1L, 20L, 100L, MessageType.TEXT, "hello")
    }
}
