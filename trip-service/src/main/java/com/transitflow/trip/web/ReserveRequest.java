package com.transitflow.trip.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReserveRequest(
        @NotBlank String bookingRef,
        @Min(1) int seatCount) {
}
