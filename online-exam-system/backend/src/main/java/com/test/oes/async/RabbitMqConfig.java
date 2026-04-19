package com.test.oes.async;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AsyncProperties.class)
public class RabbitMqConfig {

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMandatory(true);
        return template;
    }

    @Bean
    public Declarables asyncRabbitDeclarables() {
        DirectExchange domainEvents = ExchangeBuilder.directExchange(RabbitTopologyProperties.DOMAIN_EVENTS_EXCHANGE)
                .durable(true)
                .build();
        DirectExchange examCommands = ExchangeBuilder.directExchange(RabbitTopologyProperties.EXAM_COMMANDS_EXCHANGE)
                .durable(true)
                .build();
        Queue submittedQueue = mainQueue(AsyncQueues.EXAM_SUBMITTED_STATISTICS);
        Queue submittedRetryQueue = retryQueue(AsyncQueues.EXAM_SUBMITTED_STATISTICS, RabbitTopologyProperties.DOMAIN_EVENTS_EXCHANGE, AsyncRoutingKeys.EXAM_SUBMITTED);
        Queue revokedQueue = mainQueue(AsyncQueues.EXAM_REVOKED_INVALIDATE);
        Queue revokedRetryQueue = retryQueue(AsyncQueues.EXAM_REVOKED_INVALIDATE, RabbitTopologyProperties.DOMAIN_EVENTS_EXCHANGE, AsyncRoutingKeys.EXAM_REVOKED);
        Queue draftFlushQueue = mainQueue(AsyncQueues.DRAFT_FLUSH);
        Queue draftFlushRetryQueue = retryQueue(AsyncQueues.DRAFT_FLUSH, RabbitTopologyProperties.EXAM_COMMANDS_EXCHANGE, AsyncRoutingKeys.DRAFT_FLUSH);
        return new Declarables(
                domainEvents,
                examCommands,
                submittedQueue,
                BindingBuilder.bind(submittedQueue).to(domainEvents).with(AsyncRoutingKeys.EXAM_SUBMITTED),
                submittedRetryQueue,
                dlq(AsyncQueues.EXAM_SUBMITTED_STATISTICS),
                revokedQueue,
                BindingBuilder.bind(revokedQueue).to(domainEvents).with(AsyncRoutingKeys.EXAM_REVOKED),
                revokedRetryQueue,
                dlq(AsyncQueues.EXAM_REVOKED_INVALIDATE),
                draftFlushQueue,
                BindingBuilder.bind(draftFlushQueue).to(examCommands).with(AsyncRoutingKeys.DRAFT_FLUSH),
                draftFlushRetryQueue,
                dlq(AsyncQueues.DRAFT_FLUSH)
        );
    }

    private Queue mainQueue(String queueName) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", queueName + ".retry")
                .build();
    }

    private Queue retryQueue(String queueName, String exchange, String routingKey) {
        return QueueBuilder.durable(queueName + ".retry")
                .withArgument("x-message-ttl", 5000)
                .withArgument("x-dead-letter-exchange", exchange)
                .withArgument("x-dead-letter-routing-key", routingKey)
                .build();
    }

    private Queue dlq(String queueName) {
        return QueueBuilder.durable(queueName + ".dlq").build();
    }
}
