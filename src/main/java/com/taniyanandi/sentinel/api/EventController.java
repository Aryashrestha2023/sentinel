package com.taniyanandi.sentinel.api;

import com.taniyanandi.sentinel.domain.TelemetryEvent;
import com.taniyanandi.sentinel.service.EventIngestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/v1/events")
public class EventController {
    private final EventIngestionService ingestionService;

    public EventController(EventIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public ResponseEntity<Void> ingest(@Valid @RequestBody EventRequest request) {
        ingestionService.publish(new TelemetryEvent(
                request.eventId(), request.source(), request.entityId(), request.metric(),
                request.value(), request.timestamp(), request.attributes()));
        return ResponseEntity.accepted().location(URI.create("/v1/events/" + request.eventId())).build();
    }
}
