package com.taniyanandi.sentinel.domain;

import java.time.Instant;
import java.util.Map;

public record TelemetryEvent(
        String eventId,
        String source,
        String entityId,
        String metric,
        double value,
        Instant timestamp,
        Map<String, String> attributes) {
}
