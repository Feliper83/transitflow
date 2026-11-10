package com.transitflow.trip.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A hold placed on a trip's seats for a given booking. Keyed by {@code bookingRef}
 * so that a redelivered reservation request is idempotent.
 */
@Entity
@Table(name = "seat_holds")
public class SeatHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "booking_ref", nullable = false, unique = true)
    private String bookingRef;

    @Column(name = "seat_count", nullable = false)
    private int seatCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatHoldStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SeatHold() {
    }

    public SeatHold(Long tripId, String bookingRef, int seatCount) {
        this.tripId = tripId;
        this.bookingRef = bookingRef;
        this.seatCount = seatCount;
        this.status = SeatHoldStatus.HELD;
        this.createdAt = Instant.now();
    }

    public void markReleased() {
        this.status = SeatHoldStatus.RELEASED;
    }

    public boolean isHeld() {
        return status == SeatHoldStatus.HELD;
    }

    public Long getId() {
        return id;
    }

    public Long getTripId() {
        return tripId;
    }

    public String getBookingRef() {
        return bookingRef;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public SeatHoldStatus getStatus() {
        return status;
    }
}
