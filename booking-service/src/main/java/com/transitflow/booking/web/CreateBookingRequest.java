package com.transitflow.booking.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotNull Long tripId,
        @NotBlank String passengerName,
        @Min(1) int seatCount) {
}
