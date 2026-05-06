package ba.unsa.etf.admin_service.controller;

import ba.unsa.etf.admin_service.dto.*;
import ba.unsa.etf.admin_service.model.ReportStatus;
import ba.unsa.etf.admin_service.model.ReportReason;
import ba.unsa.etf.admin_service.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // POST /api/reports
    @PostMapping
    public ResponseEntity<ReportDTO.Response> create(@Valid @RequestBody ReportDTO.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.create(req));
    }

    // GET /api/reports?status=PENDING
    @GetMapping
    public ResponseEntity<List<ReportDTO.Response>> getAll(
            @RequestParam(required = false) ReportStatus status) {
        if (status != null) return ResponseEntity.ok(reportService.getByStatus(status));
        return ResponseEntity.ok(reportService.getAll());
    }

    // GET /api/reports/paged?page=0&size=10&sortBy=createdAt&direction=desc&status=PENDING&reason=SPAM
    @GetMapping("/paged")
    public ResponseEntity<PagedResponseDTO<ReportDTO.Response>> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportReason reason) {
        return ResponseEntity.ok(reportService.getAllPaged(page, size, sortBy, direction, status, reason));
    }

    // GET /api/reports/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ReportDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getById(id));
    }

    // GET /api/reports/user/{userId}/involved
    @GetMapping("/user/{userId}/involved")
    public ResponseEntity<List<ReportDTO.Response>> getInvolvingUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reportService.getInvolvingUser(userId));
    }

    // GET /api/reports/pending/old?days=7
    @GetMapping("/pending/old")
    public ResponseEntity<List<ReportDTO.Response>> getOldPending(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(reportService.getOldPendingReports(days));
    }

    // GET /api/reports/stats
    @GetMapping("/stats")
    public ResponseEntity<ReportStatsDTO.StatusCount> getStats() {
        return ResponseEntity.ok(reportService.getStats());
    }

    // PATCH /api/reports/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReportDTO.Response> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReportDTO.StatusUpdate req) {
        return ResponseEntity.ok(reportService.updateStatus(id, req));
    }

    // PATCH /api/reports/{id} — parcijalni update
    @PatchMapping("/{id}")
    public ResponseEntity<ReportDTO.Response> patch(
            @PathVariable Long id,
            @RequestBody Map<String, Object> fields) {
        return ResponseEntity.ok(reportService.patch(id, fields));
    }

    // POST /api/reports/batch
    @PostMapping("/batch")
    public ResponseEntity<ReportStatsDTO.BatchResponse> batchCreate(
            @Valid @RequestBody ReportStatsDTO.BatchRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.batchCreate(req));
    }

    // POST /api/reports/resolve-and-suspend
    @PostMapping("/resolve-and-suspend")
    public ResponseEntity<ReportDTO.Response> resolveAndSuspend(
            @Valid @RequestBody ReportStatsDTO.ResolveAndSuspendRequest req) {
        return ResponseEntity.ok(reportService.resolveAndSuspendUser(req));
    }

    // DELETE /api/reports/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reportService.delete(id);
        return ResponseEntity.noContent().build();
    }
}