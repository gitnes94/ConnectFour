package com.example.gameservice.outbox;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * The outbox table. An event row is written in the SAME transaction as the
 * domain change (the move). A separate relay later reads unpublished rows and
 * pushes them to RabbitMQ, then marks them published.
 *
 * eventId is a globally unique id (UUID) so consumers can deduplicate -
 * exactly the idempotency key the async lecture describes.
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "aggregate_id")
    private String aggregateId;   // the game id this event belongs to

    @Column(name = "event_type")
    private String eventType;     // e.g. "move-played"

    @Lob
    @Column(name = "payload", length = 2000)
    private String payload;       // JSON

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at")
    private Instant createdAt;

    public enum Status { NEW, PUBLISHED }

    protected OutboxEvent() {
    }

    public OutboxEvent(String aggregateId, String eventType, String payload) {
        this.eventId = UUID.randomUUID().toString();
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = Status.NEW;
        this.createdAt = Instant.now();
    }

    public String getEventId() { return eventId; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void markPublished() { this.status = Status.PUBLISHED; }
}
