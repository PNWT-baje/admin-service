package ba.unsa.etf.admin_service.controller;

import ba.unsa.etf.admin_service.dto.AnalyticsEventDTO;
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

    // GET /api/analytics
    @GetMapping
    public ResponseEntity<List<AnalyticsEventDTO.Response>> getAll(
            @RequestParam(required = false) EventType eventType) {
        if (eventType != null) {
            return ResponseEntity.ok(analyticsEventService.getByEventType(eventType));
        }
        return ResponseEntity.ok(analyticsEventService.getAll());
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

    // DELETE /api/analytics/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        analyticsEventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}