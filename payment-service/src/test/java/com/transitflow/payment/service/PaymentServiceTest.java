package com.transitflow.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.transitflow.payment.domain.Payment;
import com.transitflow.payment.domain.PaymentRepository;
import com.transitflow.payment.domain.PaymentStatus;
import com.transitflow.payment.messaging.PaymentResultEvent;
import com.transitflow.payment.messaging.ProcessPaymentCommand;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepository payments;

    private PaymentService service() {
        return new PaymentService(payments, new BigDecimal("500.00"));
    }

    @Test
    void approvesPaymentWithinLimit() {
        when(payments.findByBookingId(1L)).thenReturn(Optional.empty());
        var result = service().process(new ProcessPaymentCommand(1L, new BigDecimal("120.00"), "corr-1"));

        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(result.bookingId()).isEqualTo(1L);
        verify(payments).save(any(Payment.class));
    }

    @Test
    void declinesPaymentAboveLimit() {
        when(payments.findByBookingId(2L)).thenReturn(Optional.empty());
        var result = service().process(new ProcessPaymentCommand(2L, new BigDecimal("600.00"), "corr-2"));

        assertThat(result.status()).isEqualTo("FAILED");
        assertThat(result.reason()).contains("limit");
    }

    @Test
    void isIdempotentForDuplicateCommand() {
        Payment existing = new Payment(3L, new BigDecimal("120.00"), PaymentStatus.COMPLETED, null);
        when(payments.findByBookingId(3L)).thenReturn(Optional.of(existing));

        PaymentResultEvent result =
                service().process(new ProcessPaymentCommand(3L, new BigDecimal("120.00"), "corr-3"));

        assertThat(result.status()).isEqualTo("COMPLETED");
        // No second charge: save is never called for a duplicate.
        verify(payments, never()).save(any(Payment.class));
    }
}
