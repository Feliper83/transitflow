package com.transitflow.booking.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Serializes a domain event to JSON and stores it in the outbox. Must be called
 * from within the same transaction as the state change it accompanies.
 */
@Component
public class OutboxWriter {

    private final OutboxRepository outbox;
    private final ObjectMapper objectMapper;

    public OutboxWriter(OutboxRepository outbox, ObjectMapper objectMapper) {
        this.outbox = outbox;
        this.objectMapper = objectMapper;
    }

    public void write(String topic, String messageKey, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            outbox.save(new OutboxEvent(topic, messageKey, event.getClass().getSimpleName(), payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox event", e);
        }
    }
}
