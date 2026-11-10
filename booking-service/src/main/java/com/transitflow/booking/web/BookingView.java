package com.transitflow.booking.web;

import com.transitflow.booking.domain.Booking;
import java.math.BigDecimal;

public record BookingView(
        Long id,
        Long tripId,
        String passengerName,
        int seatCount,
        BigDecimal amount,
        String status,
        String failureReason) {

    public static BookingView from(Booking b) {
        return new BookingView(b.getId(), b.getTripId(), b.getPassengerName(), b.getSeatCount(),
                b.getAmount(), b.getStatus().name(), b.getFailureReason());
    }
}
