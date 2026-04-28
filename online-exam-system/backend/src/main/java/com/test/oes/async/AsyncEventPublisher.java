package com.test.oes.async;

import com.test.oes.entity.AsyncEventOutbox;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AsyncEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(AsyncEventOutbox outbox) {
        String exchange = switch (outbox.getEventType()) {
            case AsyncEventTypes.DRAFT_FLUSH -> RabbitTopologyProperties.EXAM_COMMANDS_EXCHANGE;
            default -> RabbitTopologyProperties.DOMAIN_EVENTS_EXCHANGE;
        };
        rabbitTemplate.convertAndSend(exchange, outbox.getRoutingKey(), outbox.getPayloadJson());
    }
}
