package com.transitflow.booking.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A pending outbound event, written in the same transaction as the state change
 * that produced it (ADR-0004). The relay publishes it and flips {@code sent}.
 * The payload is already serialized JSON.
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String topic;

    @Column(name = "message_key", nullable = false)
    private String messageKey;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private boolean sent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OutboxEvent() {
    }

    public OutboxEvent(String topic, String messageKey, String eventType, String payload) {
        this.id = UUID.randomUUID();
        this.topic = topic;
        this.messageKey = messageKey;
        this.eventType = eventType;
        this.payload = payload;
        this.sent = false;
        this.createdAt = Instant.now();
    }

    public void markSent() {
        this.sent = true;
    }

    public UUID getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }
}
