package com.transitflow.booking.metrics;

import com.transitflow.booking.domain.BookingStatus;
import com.transitflow.booking.outbox.OutboxRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for the booking SAGA, exposed via Micrometer at
 * {@code /actuator/prometheus}. Names are shared across the Java/Go/Python ports
 * so a single Grafana dashboard works against any of them:
 *
 * <ul>
 *   <li>{@code transitflow_bookings_created_total} — counter</li>
 *   <li>{@code transitflow_bookings_total{status=...}} — counter (CONFIRMED / CANCELLED)</li>
 *   <li>{@code transitflow_outbox_pending} — gauge of unsent outbox rows</li>
 *   <li>{@code transitflow_saga_duration_seconds} — timer/histogram, create-to-terminal latency</li>
 * </ul>
 */
@Component
public class BookingMetrics {

    private final MeterRegistry registry;
    private final Counter bookingsCreated;
    private final Timer sagaDuration;

    public BookingMetrics(MeterRegistry registry, OutboxRepository outbox) {
        this.registry = registry;

        this.bookingsCreated = Counter.builder("transitflow.bookings.created")
                .description("Total number of bookings created")
                .register(registry);

        this.sagaDuration = Timer.builder("transitflow.saga.duration")
                .description("Time from booking creation to a terminal SAGA state")
                .publishPercentileHistogram()
                .register(registry);

        Gauge.builder("transitflow.outbox.pending", outbox, OutboxRepository::countBySentFalse)
                .description("Number of outbox events still awaiting publication")
                .register(registry);
    }

    /** A new booking entered the SAGA. */
    public void bookingCreated() {
        bookingsCreated.increment();
    }

    /**
     * A booking reached a terminal state. Increments the status-tagged counter and
     * records the end-to-end SAGA duration into the histogram.
     */
    public void bookingSettled(BookingStatus status, Duration sagaDuration) {
        registry.counter("transitflow.bookings", "status", status.name()).increment();
        this.sagaDuration.record(sagaDuration);
    }
}
