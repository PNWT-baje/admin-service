package ba.unsa.etf.admin_service.controller;

import ba.unsa.etf.admin_service.dto.*;
import ba.unsa.etf.admin_service.model.EventType;
import ba.unsa.etf.admin_service.service.AnalyticsEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsEventController {

    private final AnalyticsEventService analyticsEventService;

    // POST /api/analytics
    @PostMapping
    public ResponseEntity<AnalyticsEventDTO.Response> create(@Valid @RequestBody AnalyticsEventDTO.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analyticsEventService.create(req));
    }

    // GET /api/analytics?eventType=POST_VIEW
    @GetMapping
    public ResponseEntity<List<AnalyticsEventDTO.Response>> getAll(
            @RequestParam(required = false) EventType eventType) {
        if (eventType != null) return ResponseEntity.ok(analyticsEventService.getByEventType(eventType));
        return ResponseEntity.ok(analyticsEventService.getAll());
    }

    // GET /api/analytics/paged?page=0&size=10&sortBy=createdAt&direction=desc&eventType=POST_VIEW
    @GetMapping("/paged")
    public ResponseEntity<PagedResponseDTO<AnalyticsEventDTO.Response>> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) EventType eventType) {
        return ResponseEntity.ok(analyticsEventService.getAllPaged(page, size, sortBy, direction, eventType));
    }

    // GET /api/analytics/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AnalyticsEventDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsEventService.getById(id));
    }

    // GET /api/analytics/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AnalyticsEventDTO.Response>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(analyticsEventService.getByUserId(userId));
    }

    // GET /api/analytics/recent?hours=24
    @GetMapping("/recent")
    public ResponseEntity<List<AnalyticsEventDTO.Response>> getRecent(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(analyticsEventService.getRecentEvents(hours));
    }

    // GET /api/analytics/stats
    @GetMapping("/stats")
    public ResponseEntity<AnalyticsStatsDTO.EventTypeCount> getStats() {
        return ResponseEntity.ok(analyticsEventService.getStats());
    }

    // GET /api/analytics/top-users?topN=10
    @GetMapping("/top-users")
    public ResponseEntity<List<AnalyticsStatsDTO.ActiveUser>> getTopUsers(
            @RequestParam(defaultValue = "10") int topN) {
        return ResponseEntity.ok(analyticsEventService.getMostActiveUsers(topN));
    }

    // POST /api/analytics/batch
    @PostMapping("/batch")
    public ResponseEntity<AnalyticsStatsDTO.BatchResponse> batchCreate(
            @Valid @RequestBody AnalyticsStatsDTO.BatchRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analyticsEventService.batchCreate(req));
    }

    // DELETE /api/analytics/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        analyticsEventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}