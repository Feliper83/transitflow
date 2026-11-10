package com.transitflow.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Terminal booking event consumed from {@code booking.events}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingEvent(
        Long bookingId,
        String type,
        String passengerName,
        String reason,
        String correlationId) {
}
