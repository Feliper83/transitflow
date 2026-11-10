package com.transitflow.booking.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Polls the outbox and publishes unsent events to Kafka, then marks them sent.
 * At-least-once by design: a crash after publishing but before marking sent will
 * re-publish, which downstream idempotent consumers tolerate (ADR-0005).
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxRepository outbox;
    private final KafkaTemplate<String, String> kafka;

    public OutboxRelay(OutboxRepository outbox, KafkaTemplate<String, String> kafka) {
        this.outbox = outbox;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelayString = "${transitflow.outbox.poll-interval-ms:2000}")
    @Transactional
    public void publishPending() {
        var pending = outbox.findTop100BySentFalseOrderByCreatedAtAsc();
        if (pending.isEmpty()) {
            return;
        }
        for (OutboxEvent event : pending) {
            kafka.send(event.getTopic(), event.getMessageKey(), event.getPayload());
            event.markSent();
            log.info("Relayed outbox event {} ({}) to {}",
                    event.getId(), event.getEventType(), event.getTopic());
        }
        outbox.saveAll(pending);
    }
}
