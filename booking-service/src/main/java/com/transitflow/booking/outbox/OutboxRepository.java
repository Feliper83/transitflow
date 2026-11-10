package com.transitflow.booking.outbox;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop100BySentFalseOrderByCreatedAtAsc();

    /** Current number of outbox rows still awaiting publication (drives the {@code transitflow_outbox_pending} gauge). */
    long countBySentFalse();
}
