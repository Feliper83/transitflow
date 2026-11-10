package com.transitflow.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Turns terminal booking events into passenger notifications. Here it just logs;
 * a real implementation would send email/SMS/push. Notifications are naturally
 * idempotent-tolerant (a duplicate "confirmed" message is harmless to re-log),
 * so no dedup store is needed.
 */
@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    @KafkaListener(topics = "booking.events", groupId = "notification-service")
    public void onBookingEvent(BookingEvent event) {
        MDC.put("correlationId", event.correlationId() == null ? "" : event.correlationId());
        try {
            String message = switch (event.type()) {
                case "CONFIRMED" -> "✅ Hi %s, your booking #%d is CONFIRMED. Enjoy your trip!"
                        .formatted(event.passengerName(), event.bookingId());
                case "CANCELLED" -> "❌ Hi %s, booking #%d was CANCELLED: %s"
                        .formatted(event.passengerName(), event.bookingId(), event.reason());
                default -> "ℹ️ Booking #%d update: %s".formatted(event.bookingId(), event.type());
            };
            log.info("NOTIFY -> {}", message);
        } finally {
            MDC.clear();
        }
    }
}
