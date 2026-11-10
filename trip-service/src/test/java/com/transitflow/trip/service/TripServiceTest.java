package com.transitflow.trip.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.transitflow.trip.domain.SeatHold;
import com.transitflow.trip.domain.SeatHoldRepository;
import com.transitflow.trip.domain.Trip;
import com.transitflow.trip.domain.TripRepository;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    TripRepository trips;
    @Mock
    SeatHoldRepository holds;

    private TripService service() {
        return new TripService(trips, holds);
    }

    private Trip trip(long id, int seatsAvailable) {
        Trip t = newTrip();
        setField(t, "id", id);
        setField(t, "capacity", 40);
        setField(t, "seatsAvailable", seatsAvailable);
        setField(t, "pricePerSeat", new BigDecimal("120.00"));
        setField(t, "routeName", "Test Route");
        setField(t, "departureTime", LocalDateTime.now());
        return t;
    }

    @Test
    void reservesWhenCapacityAvailable() {
        when(holds.findByBookingRef("booking-1")).thenReturn(Optional.empty());
        when(trips.findById(1L)).thenReturn(Optional.of(trip(1L, 40)));

        var result = service().reserve(1L, "booking-1", 2);

        assertThat(result.reserved()).isTrue();
        verify(holds).save(any(SeatHold.class));
    }

    @Test
    void rejectsWhenNotEnoughSeats() {
        when(holds.findByBookingRef("booking-2")).thenReturn(Optional.empty());
        when(trips.findById(1L)).thenReturn(Optional.of(trip(1L, 1)));

        var result = service().reserve(1L, "booking-2", 5);

        assertThat(result.reserved()).isFalse();
        verify(holds, never()).save(any(SeatHold.class));
    }

    @Test
    void reservationIsIdempotent() {
        SeatHold existing = new SeatHold(1L, "booking-3", 2);
        when(holds.findByBookingRef("booking-3")).thenReturn(Optional.of(existing));

        var result = service().reserve(1L, "booking-3", 2);

        assertThat(result.reserved()).isTrue();
        // Existing hold returned without touching trip inventory again.
        verify(trips, never()).save(any(Trip.class));
    }

    private static Trip newTrip() {
        try {
            var ctor = Trip.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
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
