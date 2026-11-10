package com.transitflow.trip.web;

public record ReservationResult(boolean reserved, String bookingRef, int seatCount) {
}
