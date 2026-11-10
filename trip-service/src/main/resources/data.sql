-- Seed trips. Trip 3 is priced so that a single-seat booking (600.00) exceeds
-- the payment service's decline threshold (500.00), exercising the SAGA
-- compensation path end to end.
INSERT INTO trips (id, route_name, origin, destination, departure_time, capacity, seats_available, price_per_seat, version) VALUES
  (1, 'Bogota Express',   'Bogota',    'Medellin',     '2026-12-01T08:00:00', 40, 40, 120.00, 0),
  (2, 'Caribbean Coast',  'Cartagena', 'Barranquilla', '2026-12-01T09:30:00', 30, 30,  45.00, 0),
  (3, 'Andes Premium',    'Bogota',    'Cali',         '2026-12-01T07:15:00', 20, 20, 600.00, 0);
