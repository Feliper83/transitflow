package com.transitflow.payment.service;

import com.transitflow.payment.domain.Payment;
import com.transitflow.payment.domain.PaymentRepository;
import com.transitflow.payment.domain.PaymentStatus;
import com.transitflow.payment.messaging.PaymentResultEvent;
import com.transitflow.payment.messaging.ProcessPaymentCommand;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository payments;
    private final BigDecimal declineAbove;

    public PaymentService(PaymentRepository payments,
                          @Value("${transitflow.payment.decline-above}") BigDecimal declineAbove) {
        this.payments = payments;
        this.declineAbove = declineAbove;
    }

    /**
     * Process a payment command idempotently. If a payment already exists for the
     * booking (duplicate delivery), the original outcome is returned unchanged and
     * no second charge occurs.
     */
    @Transactional
    public PaymentResultEvent process(ProcessPaymentCommand command) {
        var existing = payments.findByBookingId(command.bookingId());
        if (existing.isPresent()) {
            Payment p = existing.get();
            log.info("Duplicate payment command for booking {} ignored (already {})",
                    command.bookingId(), p.getStatus());
            return toEvent(p, command.correlationId());
        }

        boolean declined = command.amount().compareTo(declineAbove) > 0;
        Payment payment = declined
                ? new Payment(command.bookingId(), command.amount(), PaymentStatus.FAILED,
                        "Amount exceeds auto-approval limit of " + declineAbove)
                : new Payment(command.bookingId(), command.amount(), PaymentStatus.COMPLETED, null);
        payments.save(payment);

        log.info("Payment for booking {} -> {} (amount={})",
                command.bookingId(), payment.getStatus(), command.amount());
        return toEvent(payment, command.correlationId());
    }

    private PaymentResultEvent toEvent(Payment p, String correlationId) {
        return p.getStatus() == PaymentStatus.COMPLETED
                ? PaymentResultEvent.completed(p.getBookingId(), p.getAmount(), correlationId)
                : PaymentResultEvent.failed(p.getBookingId(), p.getAmount(), p.getReason(), correlationId);
    }
}
