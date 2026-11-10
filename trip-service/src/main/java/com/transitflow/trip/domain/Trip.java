package com.transitflow.trip.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_name", nullable = false)
    private String routeName;

    private String origin;
    private String destination;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    private int capacity;

    @Column(name = "seats_available", nullable = false)
    private int seatsAvailable;

    @Column(name = "price_per_seat", nullable = false)
    private BigDecimal pricePerSeat;

    /** Optimistic lock: protects seat inventory against concurrent reservations. */
    @Version
    private Long version;

    protected Trip() {
    }

    public boolean hasCapacityFor(int seats) {
        return seatsAvailable >= seats;
    }

    public void reserve(int seats) {
        if (!hasCapacityFor(seats)) {
            throw new IllegalStateException("Not enough seats available");
        }
        this.seatsAvailable -= seats;
    }

    public void release(int seats) {
        this.seatsAvailable = Math.min(capacity, this.seatsAvailable + seats);
    }

    public Long getId() {
        return id;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSeatsAvailable() {
        return seatsAvailable;
    }

    public BigDecimal getPricePerSeat() {
        return pricePerSeat;
    }
}
