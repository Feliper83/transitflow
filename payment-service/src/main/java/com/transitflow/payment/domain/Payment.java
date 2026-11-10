package com.transitflow.payment.domain;

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

/**
 * A processed payment. {@code bookingId} is unique, which is what makes the
 * consumer idempotent: a redelivered command for the same booking collides and
 * is short-circuited instead of charging twice.
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String reason;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected Payment() {
    }

    public Payment(Long bookingId, BigDecimal amount, PaymentStatus status, String reason) {
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.reason = reason;
        this.processedAt = Instant.now();
    }

    public Long getBookingId() {
        return bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }
}
