package com.transitflow.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "passenger_name", nullable = false)
    private String passengerName;

    @Column(name = "seat_count", nullable = false)
    private int seatCount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "correlation_id", nullable = false)
    private String correlationId;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Booking() {
    }

    public Booking(Long tripId, String passengerName, int seatCount, BigDecimal amount, String correlationId) {
        this.tripId = tripId;
        this.passengerName = passengerName;
        this.seatCount = seatCount;
        this.amount = amount;
        this.correlationId = correlationId;
        this.status = BookingStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void markSeatReserved() {
        transitionTo(BookingStatus.SEAT_RESERVED);
    }

    public void confirm() {
        transitionTo(BookingStatus.CONFIRMED);
    }

    public void cancel(String reason) {
        this.failureReason = reason;
        transitionTo(BookingStatus.CANCELLED);
    }

    private void transitionTo(BookingStatus next) {
        this.status = next;
        this.updatedAt = Instant.now();
    }

    public boolean isTerminal() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.CANCELLED;
    }

    /** The reference used as the idempotency key for the seat hold in trip-service. */
    public String bookingRef() {
        return "booking-" + id;
    }

    public Long getId() {
        return id;
    }

    public Long getTripId() {
        return tripId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
