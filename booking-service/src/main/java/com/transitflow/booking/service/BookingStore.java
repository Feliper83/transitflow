package com.transitflow.booking.service;

import com.transitflow.booking.domain.Booking;
import com.transitflow.booking.domain.BookingRepository;
import com.transitflow.booking.domain.BookingStatus;
import com.transitflow.booking.messaging.BookingEvent;
import com.transitflow.booking.messaging.ProcessPaymentCommand;
import com.transitflow.booking.metrics.BookingMetrics;
import com.transitflow.booking.outbox.OutboxWriter;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional persistence operations for the booking SAGA. Kept in a dedicated
 * bean so each method runs through Spring's transactional proxy — the state
 * change and its outbox write (ADR-0004) commit atomically. The orchestrator,
 * which performs the remote calls, invokes these across the bean boundary.
 */
@Service
public class BookingStore {

    static final String PAYMENT_COMMANDS_TOPIC = "payment.commands";
    static final String BOOKING_EVENTS_TOPIC = "booking.events";

    private static final Logger log = LoggerFactory.getLogger(BookingStore.class);

    private final BookingRepository bookings;
    private final OutboxWriter outbox;
    private final BookingMetrics metrics;

    public BookingStore(BookingRepository bookings, OutboxWriter outbox, BookingMetrics metrics) {
        this.bookings = bookings;
        this.outbox = outbox;
        this.metrics = metrics;
    }

    private static Duration sagaDurationOf(Booking booking) {
        return Duration.between(booking.getCreatedAt(), Instant.now());
    }

    @Transactional(readOnly = true)
    public Booking get(Long id) {
        return bookings.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
    }

    @Transactional
    public Booking createPending(Long tripId, String passengerName, int seatCount,
                                 BigDecimal amount, String correlationId) {
        Booking booking = bookings.save(
                new Booking(tripId, passengerName, seatCount, amount, correlationId));
        metrics.bookingCreated();
        log.info("Created booking {} (PENDING) for trip {}", booking.getId(), tripId);
        return booking;
    }

    @Transactional
    public void reserveAndRequestPayment(Long bookingId) {
        Booking booking = get(bookingId);
        booking.markSeatReserved();
        outbox.write(PAYMENT_COMMANDS_TOPIC, String.valueOf(bookingId),
                new ProcessPaymentCommand(bookingId, booking.getAmount(), booking.getCorrelationId()));
        log.info("Booking {} seat reserved; payment requested", bookingId);
    }

    @Transactional
    public void cancelNoSeat(Long bookingId) {
        Booking booking = get(bookingId);
        booking.cancel("No seats available");
        outbox.write(BOOKING_EVENTS_TOPIC, String.valueOf(bookingId),
                BookingEvent.cancelled(bookingId, booking.getPassengerName(),
                        booking.getFailureReason(), booking.getCorrelationId()));
        metrics.bookingSettled(BookingStatus.CANCELLED, sagaDurationOf(booking));
        log.info("Booking {} cancelled: no seats available", bookingId);
    }

    @Transactional
    public void confirm(Long bookingId) {
        Booking booking = get(bookingId);
        if (booking.isTerminal()) {
            log.info("Booking {} already {}, ignoring duplicate payment result", bookingId, booking.getStatus());
            return;
        }
        booking.confirm();
        outbox.write(BOOKING_EVENTS_TOPIC, String.valueOf(bookingId),
                BookingEvent.confirmed(bookingId, booking.getPassengerName(), booking.getCorrelationId()));
        metrics.bookingSettled(BookingStatus.CONFIRMED, sagaDurationOf(booking));
        log.info("Booking {} CONFIRMED", bookingId);
    }

    @Transactional
    public void cancelAfterPaymentFailure(Long bookingId, String reason) {
        Booking booking = get(bookingId);
        booking.cancel(reason);
        outbox.write(BOOKING_EVENTS_TOPIC, String.valueOf(bookingId),
                BookingEvent.cancelled(bookingId, booking.getPassengerName(), reason, booking.getCorrelationId()));
        metrics.bookingSettled(BookingStatus.CANCELLED, sagaDurationOf(booking));
        log.info("Booking {} CANCELLED after payment failure: {}", bookingId, reason);
    }
}
