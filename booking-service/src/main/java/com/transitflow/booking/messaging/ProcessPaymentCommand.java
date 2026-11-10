package com.transitflow.booking.messaging;

import java.math.BigDecimal;

/** Command produced to {@code payment.commands} to request payment for a booking. */
public record ProcessPaymentCommand(
        Long bookingId,
        BigDecimal amount,
        String correlationId) {
}
