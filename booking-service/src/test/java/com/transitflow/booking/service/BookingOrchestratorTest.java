package com.transitflow.booking.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.transitflow.booking.client.TripClient;
import com.transitflow.booking.domain.Booking;
import com.transitflow.booking.domain.BookingStatus;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingOrchestratorTest {

    @Mock
    BookingStore store;
    @Mock
    TripClient tripClient;

    private BookingOrchestrator orchestrator() {
        return new BookingOrchestrator(store, tripClient);
    }

    private Booking booking(Long id, BookingStatus status) {
        Booking b = new Booking(1L, "Ada", 1, new BigDecimal("120.00"), "corr");
        setField(b, "id", id);
        setField(b, "status", status);
        return b;
    }

    @Test
    void compensationReleasesSeatThenCancels() {
        when(store.get(10L)).thenReturn(booking(10L, BookingStatus.SEAT_RESERVED));

        orchestrator().compensate(10L, "payment declined");

        verify(tripClient).release("booking-10");
        verify(store).cancelAfterPaymentFailure(10L, "payment declined");
    }

    @Test
    void compensationIsSkippedForTerminalBooking() {
        when(store.get(11L)).thenReturn(booking(11L, BookingStatus.CONFIRMED));

        orchestrator().compensate(11L, "late payment failure");

        // Already terminal: no seat release, no state change.
        verify(tripClient, never()).release(anyString());
        verify(store, never()).cancelAfterPaymentFailure(eq(11L), anyString());
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
