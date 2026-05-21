package ba.unsa.etf.admin_service.controller;

import ba.unsa.etf.admin_service.dto.*;
import ba.unsa.etf.admin_service.model.EventType;
import ba.unsa.etf.admin_service.service.AnalyticsEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsEventController {

    private final AnalyticsEventService analyticsEventService;

    /** Log a single analytics event — admin and analyst. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<AnalyticsEventDTO.Response> create(@Valid @RequestBody AnalyticsEventDTO.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analyticsEventService.create(req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<AnalyticsEventDTO.Response>> getAll(
            @RequestParam(required = false) EventType eventType) {
        if (eventType != null) return ResponseEntity.ok(analyticsEventService.getByEventType(eventType));
        return ResponseEntity.ok(analyticsEventService.getAll());
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<PagedResponseDTO<AnalyticsEventDTO.Response>> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) EventType eventType) {
        return ResponseEntity.ok(analyticsEventService.getAllPaged(page, size, sortBy, direction, eventType));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<AnalyticsEventDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsEventService.getById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<AnalyticsEventDTO.Response>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(analyticsEventService.getByUserId(userId));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<AnalyticsEventDTO.Response>> getRecent(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(analyticsEventService.getRecentEvents(hours));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<AnalyticsStatsDTO.EventTypeCount> getStats() {
        return ResponseEntity.ok(analyticsEventService.getStats());
    }

    @GetMapping("/top-users")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<AnalyticsStatsDTO.ActiveUser>> getTopUsers(
            @RequestParam(defaultValue = "10") int topN) {
        return ResponseEntity.ok(analyticsEventService.getMostActiveUsers(topN));
    }

    /** Batch insert — admin-only mass import. */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsStatsDTO.BatchResponse> batchCreate(
            @Valid @RequestBody AnalyticsStatsDTO.BatchRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analyticsEventService.batchCreate(req));
    }

    /** Delete analytics event permanently — admin only. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        analyticsEventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
