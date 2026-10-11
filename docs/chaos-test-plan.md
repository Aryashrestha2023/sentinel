# Chaos Test Plan

## Scenario A — detector restart

1. Generate events.
2. Confirm the Streams application has processed them.
3. Stop the Sentinel process.
4. Restart Sentinel.
5. Confirm the state store is restored and processing resumes.
6. Confirm no unexpected gap in Kafka offsets.

## Scenario B — PostgreSQL outage

1. Generate an anomaly.
2. Stop PostgreSQL.
3. Observe the alert persistence consumer's retry/error behaviour.
4. Restart PostgreSQL.
5. Confirm the alert is eventually persisted.

## Scenario C — duplicate alert delivery

1. Capture an alert payload.
2. Publish the same alert again to `sentinel.alerts`.
3. Confirm PostgreSQL does not create a second row with the same `alert_id`.

## Scenario D — broker interruption

1. Stop Kafka.
2. Attempt event ingestion.
3. Observe producer failures.
4. Restart Kafka.
5. Confirm the application reconnects and subsequent events are processed.

Record actual timings and observations before making performance/reliability claims.
