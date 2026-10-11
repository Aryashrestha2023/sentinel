# Kubernetes deployment notes

The manifest demonstrates the application-level deployment concerns: multiple replicas, health probes, resource requests/limits and externalized infrastructure endpoints.

For a real cluster, provision Kafka/PostgreSQL/Redis with managed services or operators rather than using this application manifest as a complete production platform.
