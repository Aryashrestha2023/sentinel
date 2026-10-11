# Architecture Notes

## Data flow

1. Client sends telemetry to `POST /v1/events`.
2. API validates the payload and publishes it to `sentinel.events`.
3. `entityId` is the Kafka key.
4. Kafka Streams consumes the topic and changes the processing key to `entityId|metric`.
5. A persistent state store holds the online baseline.
6. The detector evaluates the current observation against the previous baseline.
7. Only anomalous observations are written to `sentinel.alerts`.
8. A separate consumer persists alerts to PostgreSQL.
9. REST endpoints query durable alert history.

## Consistency boundaries

Kafka Streams state is transactional within the Streams topology. PostgreSQL is an external system and therefore uses an idempotent sink pattern rather than pretending to participate in the Kafka transaction.

This distinction is deliberate and should be discussed in interviews.

## Scaling model

With six partitions, six stream tasks can process independent entity keys in parallel. Increasing partitions increases parallelism, subject to broker and consumer capacity. A future deployment can run multiple Sentinel instances in the same Kafka Streams application group.

## Hot-key trade-off

Partitioning by entity preserves ordering for an entity, but an unusually hot entity can create a skewed partition. A production design could introduce sub-keys for entities where ordering can be relaxed, or use a dedicated routing strategy for known hot keys.
