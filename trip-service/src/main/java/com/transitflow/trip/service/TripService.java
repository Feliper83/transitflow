package com.transitflow.trip.service;

import com.transitflow.trip.domain.SeatHold;
import com.transitflow.trip.domain.SeatHoldRepository;
import com.transitflow.trip.domain.Trip;
import com.transitflow.trip.domain.TripRepository;
import com.transitflow.trip.web.ReservationResult;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TripService {

    private static final Logger log = LoggerFactory.getLogger(TripService.class);

    private final TripRepository trips;
    private final SeatHoldRepository holds;

    public TripService(TripRepository trips, SeatHoldRepository holds) {
        this.trips = trips;
        this.holds = holds;
    }

    @Transactional(readOnly = true)
    public List<Trip> listTrips() {
        return trips.findAll();
    }

    @Transactional(readOnly = true)
    public Trip getTrip(Long id) {
        return trips.findById(id)
                .orElseThrow(() -> new TripNotFoundException(id));
    }

    /**
     * Reserve seats for a booking. Idempotent on {@code bookingRef}: a repeated
     * request for a reference that already holds seats returns the existing hold
     * without decrementing inventory again.
     */
    @Transactional
    public ReservationResult reserve(Long tripId, String bookingRef, int seatCount) {
        var existing = holds.findByBookingRef(bookingRef);
        if (existing.isPresent()) {
            SeatHold hold = existing.get();
            log.info("Reservation {} already exists (status={}), returning idempotently",
                    bookingRef, hold.getStatus());
            return new ReservationResult(hold.isHeld(), hold.getBookingRef(), hold.getSeatCount());
        }

        Trip trip = getTrip(tripId);
        if (!trip.hasCapacityFor(seatCount)) {
            log.info("Reservation {} rejected: trip {} has {} seats, {} requested",
                    bookingRef, tripId, trip.getSeatsAvailable(), seatCount);
            return new ReservationResult(false, bookingRef, seatCount);
        }

        trip.reserve(seatCount);
        trips.save(trip);
        holds.save(new SeatHold(tripId, bookingRef, seatCount));
        log.info("Reserved {} seat(s) on trip {} for {}", seatCount, tripId, bookingRef);
        return new ReservationResult(true, bookingRef, seatCount);
    }

    /**
     * Compensating action for a failed booking SAGA: release a previously held
     * seat. Idempotent — releasing an already-released or unknown hold is a no-op.
     */
    @Transactional
    public void release(String bookingRef) {
        var maybeHold = holds.findByBookingRef(bookingRef);
        if (maybeHold.isEmpty()) {
            log.info("Release {} ignored: no hold found", bookingRef);
            return;
        }
        SeatHold hold = maybeHold.get();
        if (!hold.isHeld()) {
            log.info("Release {} ignored: already released", bookingRef);
            return;
        }
        Trip trip = getTrip(hold.getTripId());
        trip.release(hold.getSeatCount());
        trips.save(trip);
        hold.markReleased();
        holds.save(hold);
        log.info("Released {} seat(s) on trip {} for {}", hold.getSeatCount(), trip.getId(), bookingRef);
    }
}
