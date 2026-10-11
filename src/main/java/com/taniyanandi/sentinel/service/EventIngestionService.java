package com.taniyanandi.sentinel.service;

import com.taniyanandi.sentinel.domain.TelemetryEvent;
import com.taniyanandi.sentinel.metrics.SentinelMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventIngestionService {
    private final KafkaTemplate<String, TelemetryEvent> kafkaTemplate;
    private final SentinelMetrics metrics;
    private final String topic;

    public EventIngestionService(KafkaTemplate<String, TelemetryEvent> kafkaTemplate, SentinelMetrics metrics,
                                 @Value("${sentinel.kafka.events-topic:sentinel.events}") String topic) {
        this.kafkaTemplate = kafkaTemplate; this.metrics = metrics; this.topic = topic;
    }

    public void publish(TelemetryEvent event) {
        // Entity ID is the partition key: events for an entity remain ordered within a partition.
        kafkaTemplate.send(topic, event.entityId(), event);
        metrics.eventIngested();
    }
}
