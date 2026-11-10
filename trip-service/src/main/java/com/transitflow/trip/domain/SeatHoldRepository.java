package com.transitflow.trip.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {
    Optional<SeatHold> findByBookingRef(String bookingRef);
}
