package com.transitflow.booking.messaging;

import com.transitflow.booking.service.BookingOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Consumes payment outcomes and advances the SAGA to CONFIRMED or compensation. */
@Component
public class PaymentResultListener {

    public static final String PAYMENT_EVENTS_TOPIC = "payment.events";

    private static final Logger log = LoggerFactory.getLogger(PaymentResultListener.class);

    private final BookingOrchestrator orchestrator;

    public PaymentResultListener(BookingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @KafkaListener(topics = PAYMENT_EVENTS_TOPIC, groupId = "booking-service")
    public void onPaymentResult(PaymentResultEvent event) {
        MDC.put("correlationId", event.correlationId() == null ? "" : event.correlationId());
        try {
            log.info("Payment result for booking {}: {}", event.bookingId(), event.status());
            if (event.isCompleted()) {
                orchestrator.confirm(event.bookingId());
            } else {
                orchestrator.compensate(event.bookingId(), event.reason());
            }
        } finally {
            MDC.clear();
        }
    }
}
