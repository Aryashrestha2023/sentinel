package com.taniyanandi.sentinel.service;

import com.taniyanandi.sentinel.domain.AnomalyAlert;
import com.taniyanandi.sentinel.domain.DetectionState;
import com.taniyanandi.sentinel.domain.TelemetryEvent;
import com.taniyanandi.sentinel.metrics.SentinelMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AnomalyDetectorTest {
    @Test
    void emitsCriticalAlertForLargeDeviation() {
        SentinelMetrics metrics = new SentinelMetrics(new SimpleMeterRegistry());
        AnomalyDetector detector = new AnomalyDetector(5, 3.0, metrics);
        DetectionState state = new DetectionState();

        for (int i = 0; i < 5; i++) {
            detector.evaluate(event(i + 1), state);
        }

        AnomalyAlert alert = detector.evaluate(event(100), state);

        assertThat(alert).isNotNull();
        assertThat(alert.severity()).isIn(AnomalyAlert.Severity.HIGH, AnomalyAlert.Severity.CRITICAL);
        assertThat(alert.zScore()).isGreaterThan(3.0);
    }

    private TelemetryEvent event(double value) {
        return new TelemetryEvent("e-" + value, "test", "entity-1", "latency_ms", value, Instant.now(), Map.of());
    }
}
