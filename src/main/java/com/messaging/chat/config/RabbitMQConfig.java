package com.messaging.chat.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.notification-queue}")
    private String queueName;

    @Value("${rabbitmq.notification-exchange}")
    private String exchangeName;

    @Value("${rabbitmq.notification-routing-key}")
    private String routingKey;

    @Value("${rabbitmq.notification-dlq}")
    private String deadLetterQueueName;

    @Value("${rabbitmq.notification-dlx}")
    private String deadLetterExchangeName;

    private static final String DEAD_LETTER_ROUTING_KEY = "notificationDlqRouting";


    @Bean
    public Queue queue() {
        return QueueBuilder.nonDurable(queueName)
                .deadLetterExchange(deadLetterExchangeName)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(deadLetterQueueName, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchangeName);
    }

    @Bean
    public Binding deadLetterBinding(
            Queue deadLetterQueue,
            DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
