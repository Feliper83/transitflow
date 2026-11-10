# ADR-0004: Transactional outbox for reliable event publishing

- **Status:** Accepted
- **Date:** 2026-11-09

## Context

When `booking-service` changes a booking's state it must also publish an event (e.g. `BookingConfirmed`). Doing these as two independent operations — commit to PostgreSQL, then produce to Kafka — is a **dual-write**, and dual-writes are unsafe: a crash between them either loses the event (DB committed, Kafka not) or, if ordered the other way, publishes an event for a change that later rolled back.

## Decision

Adopt the **transactional outbox pattern**. When the orchestrator changes state, it inserts the outbound event into an `outbox` table **within the same local database transaction**. A separate **relay** (a scheduled poller) reads unsent outbox rows, publishes them to Kafka, and marks them sent.

The database transaction is the single source of truth. Kafka is brought into agreement asynchronously, at-least-once (duplicates are handled by idempotent consumers — ADR-0005).

## Consequences

- **Positive:** no lost or phantom events; state and its event are atomic.
- **Positive:** survives Kafka being briefly unavailable — events accumulate in the outbox and drain when it recovers.
- **Negative:** adds a polling relay and a few seconds of publish latency. Acceptable here; a production system might use Debezium/CDC to tail the outbox instead of polling.
- **Negative:** the relay can publish a row twice if it crashes after producing but before marking it sent — which is exactly why consumers must be idempotent.
