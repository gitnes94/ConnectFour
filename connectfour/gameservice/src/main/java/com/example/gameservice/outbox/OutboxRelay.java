package com.example.gameservice.outbox;

import com.example.gameservice.config.RabbitConfig;
import com.example.gameservice.repository.OutboxRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The Outbox Relay. Runs on a schedule, reads NEW outbox rows, publishes each
 * to RabbitMQ, and marks it PUBLISHED. Because publishing is decoupled from the
 * original transaction, a crash never loses an event: unpublished rows are
 * simply picked up on the next run. This is the producer side of
 * at-least-once delivery from the async lecture.
 */
@Component
public class OutboxRelay {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxRelay(OutboxRepository outboxRepository, RabbitTemplate rabbitTemplate) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    public void publishPending() {
        var pending = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxEvent.Status.NEW);
        for (OutboxEvent event : pending) {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY,
                    event.getPayload(),
                    message -> {
                        // carry the unique id so consumers can deduplicate
                        message.getMessageProperties().setMessageId(event.getEventId());
                        message.getMessageProperties().setHeader("eventType", event.getEventType());
                        return message;
                    });
            event.markPublished();
        }
        if (!pending.isEmpty()) {
            outboxRepository.saveAll(pending);
        }
    }
}
