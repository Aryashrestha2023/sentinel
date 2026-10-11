# Building Sentinel

## Local build

```bash
./mvnw clean verify
```

If the Maven wrapper is not present in your environment, use Maven 3.9+:

```bash
mvn clean verify
```

## Docker

```bash
docker compose up -d
mvn spring-boot:run
```

## What to capture for a portfolio demo

- Kafka topic partition count
- event ingestion rate from k6
- p95/p99 HTTP latency
- detector processing latency
- anomaly count
- behaviour during detector restart
- behaviour during PostgreSQL outage

Only publish measurements you actually reproduce.
