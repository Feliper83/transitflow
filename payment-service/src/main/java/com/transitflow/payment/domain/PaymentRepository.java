package com.transitflow.payment.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);
}
