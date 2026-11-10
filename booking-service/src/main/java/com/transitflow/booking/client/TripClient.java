package com.transitflow.booking.client;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Synchronous client for trip-service: seat reservation and its compensation. */
@Component
public class TripClient {

    private static final Logger log = LoggerFactory.getLogger(TripClient.class);

    private final RestClient restClient;

    public TripClient(RestClient tripRestClient) {
        this.restClient = tripRestClient;
    }

    public record TripView(Long id, String routeName, int seatsAvailable, BigDecimal pricePerSeat) {
    }

    private record ReserveRequest(String bookingRef, int seatCount) {
    }

    public TripView getTrip(Long tripId) {
        return restClient.get()
                .uri("/api/trips/{id}", tripId)
                .retrieve()
                .body(TripView.class);
    }

    /** @return true if the seat was held, false if the trip had no capacity. */
    public boolean reserve(Long tripId, String bookingRef, int seatCount) {
        return Boolean.TRUE.equals(restClient.post()
                .uri("/api/trips/{id}/reservations", tripId)
                .body(new ReserveRequest(bookingRef, seatCount))
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return true;
                    }
                    if (response.getStatusCode().value() == 409) {
                        return false;
                    }
                    throw new IllegalStateException(
                            "trip-service reservation failed: " + response.getStatusCode());
                }));
    }

    public void release(String bookingRef) {
        restClient.post()
                .uri("/api/trips/reservations/{ref}/release", bookingRef)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.warn("Seat release for {} returned {}", bookingRef, res.getStatusCode());
                })
                .toBodilessEntity();
    }
}
