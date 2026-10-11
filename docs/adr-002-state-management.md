# ADR-002: Kafka Streams state store for detector state

## Decision

Store the online baseline in a Kafka Streams persistent key-value store.

## Why

The detector needs the previous baseline for every entity/metric. A local state store avoids a remote database round trip per event and Kafka Streams can restore state from its changelog after task movement or restart.

## Trade-offs

- State is local to the stream task and must be restored after a new assignment.
- Large state requires careful disk sizing and retention planning.
- External consumers should not treat the state store as the durable alert history; PostgreSQL serves that query use case.
