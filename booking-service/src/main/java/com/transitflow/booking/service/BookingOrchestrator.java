package com.transitflow.booking.service;

import com.transitflow.booking.client.TripClient;
import com.transitflow.booking.domain.Booking;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Drives the booking SAGA (ADR-0003). Remote calls to trip-service happen here,
 * outside any database transaction; the atomic state-change + outbox writes are
 * delegated to {@link BookingStore} so they run through the transactional proxy.
 */
@Service
public class BookingOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(BookingOrchestrator.class);

    private final BookingStore store;
    private final TripClient tripClient;

    public BookingOrchestrator(BookingStore store, TripClient tripClient) {
        this.store = store;
        this.tripClient = tripClient;
    }

    public Booking get(Long id) {
        return store.get(id);
    }

    /** Entry point: create a booking and run it through the SAGA. */
    public Booking book(Long tripId, String passengerName, int seatCount, String correlationId) {
        TripClient.TripView trip = tripClient.getTrip(tripId);
        BigDecimal amount = trip.pricePerSeat().multiply(BigDecimal.valueOf(seatCount));

        Booking booking = store.createPending(tripId, passengerName, seatCount, amount, correlationId);
        Long bookingId = booking.getId();

        // Step 1 (synchronous): hold the seat.
        boolean reserved = tripClient.reserve(tripId, booking.bookingRef(), seatCount);
        if (!reserved) {
            store.cancelNoSeat(bookingId);
        } else {
            // Step 2: request payment asynchronously via the outbox.
            store.reserveAndRequestPayment(bookingId);
        }
        return store.get(bookingId);
    }

    /** Payment succeeded — confirm (idempotent). */
    public void confirm(Long bookingId) {
        store.confirm(bookingId);
    }

    /**
     * Payment failed — compensate by releasing the seat (remote), then record the
     * cancellation. Idempotent: a booking already in a terminal state is skipped.
     */
    public void compensate(Long bookingId, String reason) {
        Booking booking = store.get(bookingId);
        if (booking.isTerminal()) {
            log.info("Booking {} already {}, skipping compensation", bookingId, booking.getStatus());
            return;
        }
        tripClient.release(booking.bookingRef());
        store.cancelAfterPaymentFailure(bookingId, reason);
    }
}
