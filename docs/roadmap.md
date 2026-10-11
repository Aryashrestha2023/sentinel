# Sentinel Roadmap

## V2 — included

- Durable PostgreSQL alert history
- Micrometer/Prometheus metrics
- Dockerized infrastructure
- Stateful Kafka Streams detection
- Idempotent alert persistence
- Architecture decision records
- Chaos test plan

## V3 — high-value extensions

### Schema evolution
Introduce Avro/Protobuf + Schema Registry for explicit event contracts and compatibility checks.

### Better anomaly models
Add EWMA, Median Absolute Deviation (MAD), seasonality-aware baselines and model selection per metric.

### Replay API
Add an administrative replay workflow that copies a bounded time range into a replay topic with an isolated Streams application ID.

### OpenTelemetry
Trace ingestion → Kafka metadata → detector → persistence for cross-service latency analysis.

### Kubernetes
Deploy multiple Sentinel instances with readiness/liveness probes and autoscaling based on CPU plus Kafka consumer lag.

### AI operations assistant
Add a read-only operations layer that can summarize anomaly clusters, retrieve service runbooks and explain alert history. It must remain outside the critical detection path.
