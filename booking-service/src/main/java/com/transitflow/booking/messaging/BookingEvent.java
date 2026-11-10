package com.transitflow.booking.messaging;

/**
 * Event produced to {@code booking.events} on a terminal booking state, consumed
 * by notification-service. {@code type} is CONFIRMED or CANCELLED.
 */
public record BookingEvent(
        Long bookingId,
        String type,
        String passengerName,
        String reason,
        String correlationId) {

    public static BookingEvent confirmed(Long bookingId, String passengerName, String correlationId) {
        return new BookingEvent(bookingId, "CONFIRMED", passengerName, null, correlationId);
    }

    public static BookingEvent cancelled(Long bookingId, String passengerName, String reason, String correlationId) {
        return new BookingEvent(bookingId, "CANCELLED", passengerName, reason, correlationId);
    }
}
