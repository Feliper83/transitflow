# ADR-0002: Event-driven microservices with database-per-service

- **Status:** Accepted
- **Date:** 2026-11-09

## Context

TransitFlow spans three bounded contexts — trips, bookings, payments — with different scaling profiles, failure modes, and rates of change. A single shared database would couple them: a schema migration for payments could break bookings, and one slow query could exhaust a connection pool shared by all.

## Decision

Each service **owns its own database** and is the sole writer to it. Services never reach into each other's tables. They integrate through:

1. **Synchronous REST** where an immediate, strongly-consistent answer is required (seat reservation).
2. **Asynchronous events over Apache Kafka** for everything that can be eventually consistent (payments, notifications).

Kafka runs in **KRaft mode** (no ZooKeeper) to keep the local footprint small.

## Consequences

- **Positive:** services deploy, scale, and fail independently; a payment outage degrades gracefully instead of taking down bookings.
- **Positive:** the event log is a natural integration and audit surface.
- **Negative:** we trade ACID-across-services for eventual consistency, which forces the SAGA, outbox, and idempotency machinery in later ADRs. This complexity is the price of independence and is only justified because the contexts genuinely differ. For a single small team with uniform scaling needs, a modular monolith would be the better call.
