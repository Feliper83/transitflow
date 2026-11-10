# ADR-0001: Record architecture decisions

- **Status:** Accepted
- **Date:** 2026-11-09

## Context

We want the *reasoning* behind TransitFlow's architecture to be durable and reviewable, not locked in someone's head. In distributed systems the "why" matters more than the "what" — most designs are defensible, but only against their stated constraints.

## Decision

We record every significant architecture decision as an **Architecture Decision Record (ADR)** — a short, immutable, numbered Markdown file in `docs/adr/`, following Michael Nygard's format (Context → Decision → Consequences). Superseded decisions are kept and marked, never deleted.

## Consequences

- New engineers can read the ADR log and understand how the system got to where it is.
- Decisions are debated in writing, in pull requests, before they calcify in code.
- The log doubles as onboarding material and interview-ready evidence of system-design thinking.
