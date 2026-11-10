package com.transitflow.trip.web;

import com.transitflow.trip.service.TripService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService service;

    public TripController(TripService service) {
        this.service = service;
    }

    @GetMapping
    public List<TripView> list() {
        return service.listTrips().stream().map(TripView::from).toList();
    }

    @GetMapping("/{id}")
    public TripView get(@PathVariable Long id) {
        return TripView.from(service.getTrip(id));
    }

    /** Reserve seats for a booking (called synchronously by booking-service). */
    @PostMapping("/{id}/reservations")
    public ResponseEntity<ReservationResult> reserve(@PathVariable Long id,
                                                     @Valid @RequestBody ReserveRequest request) {
        ReservationResult result = service.reserve(id, request.bookingRef(), request.seatCount());
        return result.reserved()
                ? ResponseEntity.ok(result)
                : ResponseEntity.status(409).body(result);
    }

    /** Release a held seat — the SAGA compensation for a failed booking. */
    @PostMapping("/reservations/{bookingRef}/release")
    public ResponseEntity<Void> release(@PathVariable String bookingRef) {
        service.release(bookingRef);
        return ResponseEntity.noContent().build();
    }
}
