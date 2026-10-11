# Sentinel

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Kafka Streams](https://img.shields.io/badge/Kafka%20Streams-stateful-black?logo=apachekafka)](https://kafka.apache.org/documentation/streams/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Hacktoberfest](https://img.shields.io/badge/Hacktoberfest-2026-blueviolet)](https://hacktoberfest.com)
[![Good First Issues](https://img.shields.io/github/issues/taniyanandi/sentinel/good%20first%20issue)](https://github.com/taniyanandi/sentinel/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22)

A distributed, stateful stream-processing engine that detects anomalies from high-volume telemetry in near real time — built the way production observability systems actually work.

> **Not a tutorial project.** Sentinel demonstrates the hard parts of event-driven backends: partition-aware ordering, stateful stream processing with exactly-once semantics, O(1)-memory online statistics, idempotent persistence, and observable infrastructure. Every design decision has an [ADR](docs/) explaining why.

---

## Architecture

```text
                     ┌──────────────────────┐
                     │  REST Event Ingestion │
                     │   Spring Boot / Java  │
                     └──────────┬───────────┘
                                │ entityId = partition key
                                ▼
                     ┌──────────────────────┐
                     │     Apache Kafka     │
                     │   sentinel.events    │
                     │    6 partitions      │
                     └──────────┬───────────┘
                                │
                       Kafka Streams topology
                                │
              ┌─────────────────▼─────────────────┐
              │     Stateful Anomaly Detector     │
              │  Welford's online mean/variance   │
              │   Persistent local state store    │
              └─────────────────┬─────────────────┘
                                │ anomalies only
                                ▼
                     ┌──────────────────────┐
                     │   sentinel.alerts    │
                     └──────────┬───────────┘
                                │
                   ┌────────────┴────────────┐
                   ▼                         ▼
          ┌────────────────┐        ┌─────────────────┐
          │  PostgreSQL    │        │  Alert REST API  │
          │ durable alerts │        │ recent / severity│
          └────────────────┘        └─────────────────┘

         Metrics: Micrometer → Prometheus → Grafana
         Cache / extension point: Redis
```

---

## Why this project exists

Most streaming tutorials skip the hard parts. Sentinel doesn't.

**1. Partition-aware ordering** — Events are keyed by `entityId`, so events for the same entity land on the same Kafka partition. This preserves the ordering the per-entity baseline calculation requires while allowing horizontal scale across partitions.

**2. O(1)-memory stateful stream processing** — The detector maintains a persistent Kafka Streams state store per `entityId|metric` key. Welford's online algorithm updates mean and variance in constant memory per key instead of retaining event history.

**3. Exactly-once with documented boundaries** — Kafka Streams runs with `exactly_once_v2`. The PostgreSQL sink is independently idempotent (keyed by `alert_id`) and is *not* part of the Kafka transaction — this distinction is deliberate and documented in [ADR-002](docs/adr-002-state-management.md).

**4. Durable, queryable alerts** — Alerts aren't held in memory. A Kafka consumer persists them to PostgreSQL with composite indexes designed for recent-alerts and severity queries.

**5. Observable by design** — Micrometer instruments ingestion volume, anomaly rate, critical anomaly rate, and detection latency. Prometheus scrapes; Grafana dashboards.

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| API | Spring Boot 3.4, REST, Bean Validation |
| Streaming | Apache Kafka, Kafka Streams |
| State management | Kafka Streams persistent state store (RocksDB) |
| Database | PostgreSQL 16, Spring Data JPA |
| Cache / extension | Redis 7 |
| Observability | Micrometer, Prometheus, Grafana, Spring Boot Actuator |
| Testing | JUnit 5, Spring Kafka Test, Testcontainers |
| Load testing | k6 (100 events/sec sustained) |
| Runtime | Docker Compose |

---

## Detection model

Sentinel uses a simple, explainable statistical approach: **online z-score detection** with Welford's algorithm.

After a configurable warm-up phase (default: 30 events per entity/metric key):

```
z = |x − μ| / σ
```

Severity tiers from the deviation:

| z-score | Severity |
|---|---|
| ≥ 3.0 | `LOW` |
| ≥ 4.0 | `MEDIUM` |
| ≥ 5.0 | `HIGH` |
| ≥ 6.0 | `CRITICAL` |

The detector is intentionally simple so it's explainable in an interview. The [roadmap](docs/roadmap.md) documents the production evolution path: EWMA, MAD, seasonality-aware baselines, and ML-based detectors.

---

## Quick start

### Prerequisites

- Docker + Docker Compose
- Java 21
- Maven 3.9+

### Start everything

```bash
git clone https://github.com/taniyanandi/sentinel.git
cd sentinel
docker compose up -d          # Kafka, PostgreSQL, Redis, Prometheus, Grafana
./mvnw spring-boot:run        # start Sentinel
```

### Ingest an event

```bash
curl -X POST http://localhost:8080/v1/events \
  -H 'Content-Type: application/json' \
  -d '{
    "eventId": "evt-1001",
    "source": "payment-service",
    "entityId": "merchant-42",
    "metric": "latency_ms",
    "value": 125.4,
    "timestamp": "2026-09-07T10:00:00Z",
    "attributes": {"region": "ap-south-1", "service": "checkout"}
  }'
```

### Query alerts

```bash
# Recent alerts
curl http://localhost:8080/v1/alerts?limit=20

# Critical alerts only
curl http://localhost:8080/v1/alerts/severity/CRITICAL?limit=20
```

### Dashboards and metrics

| Service | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Health check | `http://localhost:8080/actuator/health` |
| Prometheus metrics | `http://localhost:8080/actuator/prometheus` |
| Prometheus UI | `http://localhost:9090` |
| Grafana | `http://localhost:3000` |

### Load test

```bash
k6 run loadtest/sentinel.js   # 100 events/sec for 60s
```

### Run tests

```bash
./mvnw test   # requires Docker for Testcontainers
```

---

## API reference

| Method | Path | Description |
|---|---|---|
| `POST` | `/v1/events` | Ingest a telemetry event. Returns `202 Accepted`. |
| `GET` | `/v1/alerts` | Recent alerts, newest first. `?limit=N` (max 200) |
| `GET` | `/v1/alerts/severity/{severity}` | Filter by severity: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `GET` | `/actuator/health` | Liveness + readiness probes |
| `GET` | `/actuator/prometheus` | Prometheus scrape endpoint |

---

## Failure scenarios

See [docs/chaos-test-plan.md](docs/chaos-test-plan.md) for a repeatable demo:

1. **Detector restart** — Kill and restart the process. Verify Kafka Streams state store restoration and offset recovery.
2. **PostgreSQL outage** — Stop Postgres mid-stream. Verify alerts retry instead of being lost. Restart and confirm catch-up.
3. **Duplicate delivery** — Replay the same alert. Verify the database remains idempotent (no duplicate rows).
4. **Broker interruption** — Stop Kafka. Observe producer failures. Restart and verify reconnection.

---

## Engineering decisions

| Decision | Rationale |
|---|---|
| Kafka over synchronous calls | Absorbs bursts, decouples ingestion from detection availability |
| Entity-key partitioning | Preserves per-entity ordering while enabling horizontal parallelism |
| Kafka Streams state store | Local state with changelog recovery, no remote DB call per event |
| PostgreSQL alert sink | Durable, queryable history independent of stream-processing state |
| Explainable z-score | Deterministic, easy to reason about before introducing ML |
| Fail-soft roadmap | Advanced ML/LLM components enrich operations, never become hard dependencies |

Full reasoning in [ADR-001](docs/adr-001-partitioning.md) and [ADR-002](docs/adr-002-state-management.md).

---

## Contributing — Hacktoberfest 2026 🎃

We have [good first issues](https://github.com/taniyanandi/sentinel/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) open — each is scoped, has clear acceptance criteria, and points to the exact files to change:

| Issue | Stack | Difficulty |
|---|---|---|
| Add `GET /v1/alerts/{alertId}` — fetch a single alert | Java, Spring Boot | 🟢 Beginner |
| Add alert count endpoint by entity ID | Java, Spring Data | 🟢 Beginner |
| Add Bean Validation annotations to `EventRequest` fields | Java, Validation | 🟢 Beginner |
| Add OpenAPI/Swagger annotations to all controllers | Java, Documentation | 🟢 Beginner |
| Create a Postman/Bruno API collection | Documentation | 🟢 Beginner |
| Add a Grafana dashboard JSON for Sentinel metrics | Grafana, Observability | 🟢 Beginner |
| Add event count and throughput endpoint | Java, Metrics | 🟡 Intermediate |
| Add correlation ID filter for request tracing | Java, SLF4J MDC | 🟡 Intermediate |
| Add dead-letter topic for events that fail detection | Java, Kafka | 🟡 Intermediate |
| Add EWMA detector as an alternative to z-score | Java, Statistics | 🟡 Intermediate |
| Add alert suppression / cooldown per entity+metric | Java, Kafka Streams | 🔴 Advanced |

**First time contributing to open source?** Look for 🟢 Beginner issues — they include step-by-step instructions and point to example code already in the project. Comment on the issue to claim it before starting.

See [CONTRIBUTING.md](CONTRIBUTING.md) to get started.

---

## Repository layout

```text
sentinel/
├── src/main/java/com/taniyanandi/sentinel/
│   ├── api/            REST controllers and request DTOs
│   ├── admin/          Health and service metadata
│   ├── config/         Kafka, Redis configuration
│   ├── domain/         Immutable event and alert records
│   ├── metrics/        Micrometer instrumentation
│   ├── persistence/    PostgreSQL JPA alert store
│   ├── service/        Ingestion, anomaly detection, alert persistence
│   └── stream/         Kafka Streams topology
├── docs/
│   ├── architecture.md
│   ├── adr-001-partitioning.md
│   ├── adr-002-state-management.md
│   ├── chaos-test-plan.md
│   └── roadmap.md
├── loadtest/           k6 load test scenarios
├── infra/              Prometheus config
├── deploy/k8s/         Kubernetes manifests
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

---

## License

MIT
