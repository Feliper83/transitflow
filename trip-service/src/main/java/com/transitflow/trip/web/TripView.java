package com.transitflow.trip.web;

import com.transitflow.trip.domain.Trip;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TripView(
        Long id,
        String routeName,
        String origin,
        String destination,
        LocalDateTime departureTime,
        int seatsAvailable,
        BigDecimal pricePerSeat) {

    public static TripView from(Trip t) {
        return new TripView(t.getId(), t.getRouteName(), t.getOrigin(), t.getDestination(),
                t.getDepartureTime(), t.getSeatsAvailable(), t.getPricePerSeat());
    }
}
