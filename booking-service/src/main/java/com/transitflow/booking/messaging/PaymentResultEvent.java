package com.transitflow.booking.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Event consumed from {@code payment.events}, emitted by payment-service. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentResultEvent(
        Long bookingId,
        BigDecimal amount,
        String status,
        String reason,
        String correlationId) {

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
}
