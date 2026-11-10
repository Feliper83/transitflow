package com.transitflow.payment.messaging;

import java.math.BigDecimal;

/**
 * Event produced to {@code payment.events}. A single event type carries the
 * outcome (COMPLETED / FAILED) so consumers branch on a field instead of on the
 * message's Java type — this keeps producer and consumer decoupled.
 */
public record PaymentResultEvent(
        Long bookingId,
        BigDecimal amount,
        String status,
        String reason,
        String correlationId) {

    public static PaymentResultEvent completed(Long bookingId, BigDecimal amount, String correlationId) {
        return new PaymentResultEvent(bookingId, amount, "COMPLETED", null, correlationId);
    }

    public static PaymentResultEvent failed(Long bookingId, BigDecimal amount, String reason, String correlationId) {
        return new PaymentResultEvent(bookingId, amount, "FAILED", reason, correlationId);
    }
}
