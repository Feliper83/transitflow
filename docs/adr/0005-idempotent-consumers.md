# ADR-0005: Idempotent consumers and dead-letter queues

- **Status:** Accepted
- **Date:** 2026-11-09

## Context

Kafka delivers **at-least-once**: a consumer may process the same record more than once (a rebalance before the offset is committed, a relay re-publishing an outbox row — ADR-0004). For side-effecting consumers this is dangerous: processing `ProcessPayment` twice must not charge the passenger twice. Separately, a permanently un-processable "poison" message must not block its partition forever.

## Decision

1. **Idempotent consumers.** Every consumer that causes a side effect derives an **idempotency key** from the message (for payments, the booking id) and records processed keys. A duplicate returns the stored outcome instead of repeating the effect.
2. **Dead-letter queues.** Consumers retry a failing message a bounded number of times with backoff; on exhaustion the message is routed to a `<topic>.DLT` dead-letter topic for inspection and manual replay, and the main partition proceeds.

## Consequences

- **Positive:** correctness under redelivery — no double charges, no duplicate confirmations.
- **Positive:** a single bad message degrades to a DLT entry and an alert, not a stalled pipeline.
- **Negative:** idempotency state must be stored and eventually pruned; we keep it in each service's own database keyed by the natural business id.
- **Negative:** DLT messages need an operational process (dashboard + replay tool) that this reference implementation stubs but does not fully build.
