package com.taniyanandi.sentinel.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "anomaly_alerts", indexes = {
        @Index(name = "idx_alert_entity_metric_detected", columnList = "entity_id,metric,detected_at"),
        @Index(name = "idx_alert_severity_detected", columnList = "severity,detected_at")
})
public class AlertEntity {
    @Id
    @Column(name = "alert_id", nullable = false, updatable = false, length = 64)
    private String alertId;
    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;
    @Column(name = "entity_id", nullable = false, length = 128)
    private String entityId;
    @Column(nullable = false, length = 128)
    private String metric;
    @Column(nullable = false)
    private double value;
    @Column(name = "baseline_mean", nullable = false)
    private double baselineMean;
    @Column(name = "baseline_stddev", nullable = false)
    private double baselineStdDev;
    @Column(name = "z_score", nullable = false)
    private double zScore;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Severity severity;
    @Column(nullable = false, length = 512)
    private String reason;
    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    protected AlertEntity() {}

    public AlertEntity(String alertId, String eventId, String entityId, String metric, double value,
                        double baselineMean, double baselineStdDev, double zScore,
                        Severity severity, String reason, Instant detectedAt) {
        this.alertId = alertId; this.eventId = eventId; this.entityId = entityId; this.metric = metric;
        this.value = value; this.baselineMean = baselineMean; this.baselineStdDev = baselineStdDev;
        this.zScore = zScore; this.severity = severity; this.reason = reason; this.detectedAt = detectedAt;
    }

    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
    public String getAlertId() { return alertId; }
    public String getEventId() { return eventId; }
    public String getEntityId() { return entityId; }
    public String getMetric() { return metric; }
    public double getValue() { return value; }
    public double getBaselineMean() { return baselineMean; }
    public double getBaselineStdDev() { return baselineStdDev; }
    public double getZScore() { return zScore; }
    public Severity getSeverity() { return severity; }
    public String getReason() { return reason; }
    public Instant getDetectedAt() { return detectedAt; }
}
