package com.taniyanandi.sentinel.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<AlertEntity, String> {
    List<AlertEntity> findByOrderByDetectedAtDesc(Pageable pageable);
    List<AlertEntity> findBySeverityOrderByDetectedAtDesc(AlertEntity.Severity severity, Pageable pageable);
    long countByEntityId(String entityId);
}
