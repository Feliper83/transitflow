package com.transitflow.payment.messaging;

import java.math.BigDecimal;

/** Command consumed from {@code payment.commands}, emitted by the booking SAGA. */
public record ProcessPaymentCommand(
        Long bookingId,
        BigDecimal amount,
        String correlationId) {
}
