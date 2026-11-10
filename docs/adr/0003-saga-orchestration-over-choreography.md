# ADR-0003: Orchestrated SAGA over choreography

- **Status:** Accepted
- **Date:** 2026-11-09

## Context

A booking requires a seat reservation **and** a payment, across two services, with no distributed transaction available (ADR-0002). We need to keep the system consistent when a later step fails — e.g. the seat is held but the payment is declined. The two common patterns are:

- **Choreography:** each service listens for events and reacts; the process emerges from the chain of reactions.
- **Orchestration:** one component explicitly drives the steps and issues compensations.

## Decision

Use an **orchestrated SAGA** with `booking-service` as the orchestrator. It owns the booking state machine (`PENDING → SEAT_RESERVED → CONFIRMED | CANCELLED`) and issues compensating actions (release the seat) when a downstream step fails.

## Consequences

- **Positive:** the business process lives in one place and is explicit, testable, and easy to extend with new steps. Debugging a stuck booking means reading one state machine, not reconstructing an implicit chain across five services.
- **Positive:** compensation logic is centralized and obvious.
- **Negative:** the orchestrator is a focal point of logic and a potential coupling hotspot; we keep it thin (it coordinates, it doesn't do others' work).
- **Rejected — choreography:** with only 2–3 steps it offered no benefit and would have scattered the "what happens next" across services, which is the classic failure mode as sagas grow. Revisit if the flow fans out into many independent reactions.
