# ADR-001: Partition by entity ID

## Decision

Use `entityId` as the Kafka record key for the ingestion topic.

## Why

The anomaly baseline is calculated per entity and metric. Keeping an entity's events on the same partition gives deterministic ordering for that entity while still distributing different entities across partitions.

## Trade-offs

- A hot entity can create partition skew.
- Increasing partitions changes key-to-partition mapping for new writes but Kafka maintains ordering within each partition.
- If future algorithms require cross-entity joins, additional repartitioning may be necessary.
