package com.transitflow.trip.service;

public class TripNotFoundException extends RuntimeException {
    public TripNotFoundException(Long id) {
        super("Trip not found: " + id);
    }
}
