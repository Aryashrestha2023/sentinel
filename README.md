# Sentinel

## Distributed Real-Time Event Processing & Anomaly Detection Engine

Sentinel is a production-style streaming platform for detecting anomalous behaviour from high-volume telemetry and financial-system events in near real time.

> **Portfolio goal:** demonstrate backend engineering depth in distributed systems, event-driven architecture, stateful stream processing, reliability, observability, and ML/AI-adjacent platform design.

### Architecture

```text
                         ┌──────────────────────┐
                         │  REST Event Ingestion │
                         │   Spring Boot / Java  │
                         └──────────┬───────────┘
                                    │ entityId = partition key
                                    ▼
                         ┌──────────────────────┐
                         │        Kafka         │
                         │   sentinel.events    │
                         │      6 partitions    │
                         └──────────┬───────────┘
                                    │
                           Kafka Streams topology
                                    │
                  ┌─────────────────▼─────────────────┐
                  │     Stateful Anomaly Detector     │
                  │ Welford mean/variance + z-score   │
                  │ Persistent local state store      │
                  └─────────────────┬─────────────────┘
                                    │ anomalies only
                                    ▼
                         ┌──────────────────────┐
                         │ sentinel.alerts      │
                         └──────────┬───────────┘
                                    │
                       ┌────────────┴────────────┐
                       ▼                         ▼
              ┌────────────────┐        ┌─────────────────┐
              │ PostgreSQL     │        │ Alert REST API  │
              │ durable alerts │        │ recent/severity │
              └────────────────┘        └─────────────────┘

             Metrics: Micrometer → Prometheus → Grafana
             Cache/extension point: Redis
```

## Why this is interesting

### 1. Partition-aware ordering
Events are produced with `entityId` as the Kafka key. This keeps events for the same entity on the same partition, preserving ordering needed by the per-entity baseline calculation while allowing horizontal scale across partitions.

### 2. Stateful stream processing
Kafka Streams maintains a persistent `baseline-store` for each `entityId|metric`. The detector uses Welford's online algorithm, so mean and variance are updated in O(1) memory per key instead of retaining the complete event history.

### 3. Failure-aware processing
The Streams application is configured with `exactly_once_v2`. Kafka producer idempotence is enabled and alerts are persisted using an idempotent database write path keyed by `alert_id`.

**Important:** exactly-once guarantees apply to the Kafka Streams topology. The external PostgreSQL sink is independently idempotent and is not part of the Kafka transaction.

### 4. Alert durability
Alerts are no longer held in an in-memory list. A Kafka consumer persists them to PostgreSQL with indexes designed for recent alerts and severity queries.

### 5. Observable by design
Spring Boot Actuator exposes health and Prometheus metrics. Sentinel records ingestion volume, anomaly volume, critical anomaly volume, and detector latency.

## Tech stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| API | Spring Boot 3.4, REST, Bean Validation |
| Streaming | Apache Kafka, Kafka Streams |
| State | Kafka Streams persistent state store |
| Database | PostgreSQL 16, Spring Data JPA |
| Cache/extension point | Redis 7 |
| Observability | Micrometer, Prometheus, Grafana, Actuator |
| Testing | JUnit 5, Spring Kafka Test, Testcontainers |
| Runtime | Docker Compose |
| Load test | k6 |

## Detection model

Sentinel starts in a warm-up phase for each `entityId|metric` key. After the baseline is established:

`z = |x - μ| / σ`

Severity is derived from the deviation:

- `LOW`: z ≥ 3
- `MEDIUM`: z ≥ 4
- `HIGH`: z ≥ 5
- `CRITICAL`: z ≥ 6

The detector is intentionally simple and explainable. A production evolution path is documented in `docs/roadmap.md` for EWMA, MAD, seasonality and ML-based detectors.

## Quick start

### Prerequisites

- Docker + Docker Compose
- Java 21
- Maven 3.9+

### Start infrastructure

```bash
docker compose up -d
```

### Run Sentinel

```bash
./mvnw spring-boot:run
```

Or package it:

```bash
./mvnw clean package
java -jar target/sentinel-2.0.0.jar
```

### Ingest an event

```bash
curl -X POST http://localhost:8080/v1/events \
  -H 'Content-Type: application/json' \
  -d '{
    "eventId":"evt-1001",
    "source":"payment-service",
    "entityId":"merchant-42",
    "metric":"latency_ms",
    "value":125.4,
    "timestamp":"2026-09-07T10:00:00Z",
    "attributes":{"region":"ap-south-1","service":"checkout"}
  }'
```

### Query alerts

```bash
curl http://localhost:8080/v1/alerts?limit=20
curl http://localhost:8080/v1/alerts/severity/CRITICAL?limit=20
```

### Metrics

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/prometheus`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

## Load testing

The `loadtest/sentinel.js` scenario generates synthetic telemetry. Run it after starting Sentinel:

```bash
k6 run loadtest/sentinel.js
```

**Do not publish throughput/latency numbers in a resume until you run the test in your environment and record the actual result.**

## Failure scenarios to demonstrate

See `docs/chaos-test-plan.md` for a repeatable demo covering:

1. Kill the detector process.
2. Restart it.
3. Verify Kafka offsets/state restore processing.
4. Stop PostgreSQL.
5. Verify alert consumption retries instead of losing the source alert.
6. Restart PostgreSQL and verify durable alerts catch up.
7. Replay the same alert and verify the database remains idempotent.

## Engineering decisions

- **Kafka over direct synchronous calls:** absorbs bursts and decouples producers from detector availability.
- **Entity-key partitioning:** preserves per-entity ordering while enabling horizontal parallelism.
- **Kafka Streams state store:** local state with Kafka-backed changelog recovery rather than a remote database call per event.
- **PostgreSQL sink:** durable queryable alert history independent of the stream-processing state.
- **Explainable z-score baseline:** deterministic and easy to reason about before introducing more complex ML.
- **Fail-soft roadmap:** advanced ML/LLM components should enrich operations, never become a hard dependency for event ingestion or detection.

## Repository map

```text
src/main/java/com/taniyanandi/sentinel/
├── api/            REST contracts and controllers
├── admin/          service metadata
├── config/         Kafka/Redis configuration
├── domain/         immutable event and alert models
├── metrics/        Micrometer instrumentation
├── persistence/    PostgreSQL alert store
├── service/        ingestion + anomaly detection
└── stream/         Kafka Streams topology

docs/
├── architecture.md
├── adr-001-partitioning.md
├── adr-002-state-management.md
├── chaos-test-plan.md
└── roadmap.md
```

## Resume-ready project description

**Sentinel — Distributed Real-Time Event Processing & Anomaly Detection Engine**  
`Java 21 · Spring Boot · Kafka Streams · PostgreSQL · Redis · Prometheus · Grafana`

- Built a stateful Kafka Streams pipeline for near-real-time anomaly detection, partitioning by entity to preserve ordering while enabling horizontal processing.
- Implemented O(1)-memory online statistical baselines with persistent Kafka Streams state and configurable severity thresholds.
- Designed durable alert delivery to PostgreSQL with idempotent persistence, query indexes, Actuator/Micrometer metrics, and Prometheus observability.
- Configured Kafka producer idempotence and Kafka Streams `exactly_once_v2`, with documented boundaries between Kafka transactional guarantees and external database sinks.

## Honest portfolio rule

This repository is intentionally written to be technically defensible. Do not claim production traffic, 100K+ events/sec, sub-10ms p95 latency, or zero data loss unless you have actually measured and demonstrated those properties.
