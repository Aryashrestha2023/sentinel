package com.taniyanandi.sentinel.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/v1/system/info")
    public Map<String, Object> info() {
        return Map.of("service", "sentinel", "version", "2.0.0", "mode", "streaming", "timestamp", Instant.now());
    }
}
