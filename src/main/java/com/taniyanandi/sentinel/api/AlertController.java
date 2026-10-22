package com.taniyanandi.sentinel.api;

import com.taniyanandi.sentinel.persistence.AlertEntity;
import com.taniyanandi.sentinel.persistence.AlertRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/alerts")
public class AlertController {
    private final AlertRepository repository;
    public AlertController(AlertRepository repository) { this.repository = repository; }

    @GetMapping
    public List<AlertEntity> recent(@RequestParam(defaultValue = "50") int limit) {
        return repository.findByOrderByDetectedAtDesc(PageRequest.of(0, Math.min(Math.max(limit, 1), 200)));
    }

    @GetMapping("/severity/{severity}")
    public List<AlertEntity> bySeverity(@PathVariable AlertEntity.Severity severity,
                                        @RequestParam(defaultValue = "50") int limit) {
        return repository.findBySeverityOrderByDetectedAtDesc(severity, PageRequest.of(0, Math.min(Math.max(limit, 1), 200)));
    }

    @GetMapping("/entity/{entityId}/count")
    public Map<String, Object> countByEntityId(@PathVariable String entityId) {
        long count = repository.countByEntityId(entityId);

        return Map.of(
                "entityId", entityId,
                "alertCount", count
        );
    }
}
