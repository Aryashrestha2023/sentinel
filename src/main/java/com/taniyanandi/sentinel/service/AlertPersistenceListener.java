package com.taniyanandi.sentinel.service;

import com.taniyanandi.sentinel.domain.AnomalyAlert;
import com.taniyanandi.sentinel.persistence.AlertEntity;
import com.taniyanandi.sentinel.persistence.AlertRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AlertPersistenceListener {
    private final AlertRepository repository;
    public AlertPersistenceListener(AlertRepository repository) { this.repository = repository; }

    @Transactional
    @KafkaListener(topics = "${sentinel.kafka.alerts-topic:sentinel.alerts}", groupId = "sentinel-alert-persistence")
    public void persist(AnomalyAlert alert) {
        if (repository.existsById(alert.alertId())) return;
        repository.save(new AlertEntity(alert.alertId(), alert.eventId(), alert.entityId(), alert.metric(),
                alert.value(), alert.baselineMean(), alert.baselineStdDev(), alert.zScore(),
                AlertEntity.Severity.valueOf(alert.severity().name()), alert.reason(), alert.detectedAt()));
    }
}
