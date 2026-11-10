package com.transitflow.booking.web;

import com.transitflow.booking.domain.Booking;
import com.transitflow.booking.service.BookingOrchestrator;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingOrchestrator orchestrator;

    public BookingController(BookingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BookingView create(@Valid @RequestBody CreateBookingRequest request,
                              @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        String corrId = (correlationId == null || correlationId.isBlank())
                ? UUID.randomUUID().toString()
                : correlationId;
        MDC.put("correlationId", corrId);
        try {
            Booking booking = orchestrator.book(
                    request.tripId(), request.passengerName(), request.seatCount(), corrId);
            return BookingView.from(booking);
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingView> get(@PathVariable Long id) {
        return ResponseEntity.ok(BookingView.from(orchestrator.get(id)));
    }
}
