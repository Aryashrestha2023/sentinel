package com.taniyanandi.sentinel.domain;

import java.time.Instant;

public record AnomalyAlert(
        String alertId,
        String eventId,
        String entityId,
        String metric,
        double value,
        double baselineMean,
        double baselineStdDev,
        double zScore,
        Severity severity,
        String reason,
        Instant detectedAt) {
    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
}
