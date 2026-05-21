package ba.unsa.etf.admin_service.controller;

import ba.unsa.etf.admin_service.dto.*;
import ba.unsa.etf.admin_service.model.ReportStatus;
import ba.unsa.etf.admin_service.model.ReportReason;
import ba.unsa.etf.admin_service.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** Any authenticated user can report content. */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportDTO.Response> create(@Valid @RequestBody ReportDTO.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.create(req));
    }

    /** List all reports — moderators and above. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<ReportDTO.Response>> getAll(
            @RequestParam(required = false) ReportStatus status) {
        if (status != null) return ResponseEntity.ok(reportService.getByStatus(status));
        return ResponseEntity.ok(reportService.getAll());
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<PagedResponseDTO<ReportDTO.Response>> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportReason reason) {
        return ResponseEntity.ok(reportService.getAllPaged(page, size, sortBy, direction, status, reason));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ReportDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getById(id));
    }

    @GetMapping("/user/{userId}/involved")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<ReportDTO.Response>> getInvolvingUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reportService.getInvolvingUser(userId));
    }

    @GetMapping("/pending/old")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<ReportDTO.Response>> getOldPending(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(reportService.getOldPendingReports(days));
    }

    /** Statistics — analysts can view aggregate stats without accessing individual reports. */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'ANALYST')")
    public ResponseEntity<ReportStatsDTO.StatusCount> getStats() {
        return ResponseEntity.ok(reportService.getStats());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ReportDTO.Response> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReportDTO.StatusUpdate req) {
        return ResponseEntity.ok(reportService.updateStatus(id, req));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ReportDTO.Response> patch(
            @PathVariable Long id,
            @RequestBody Map<String, Object> fields) {
        return ResponseEntity.ok(reportService.patch(id, fields));
    }

    /** Batch create — admin-only mass import. */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportStatsDTO.BatchResponse> batchCreate(
            @Valid @RequestBody ReportStatsDTO.BatchRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.batchCreate(req));
    }

    /** Combined resolve + suspend — moderators and above. */
    @PostMapping("/resolve-and-suspend")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ReportDTO.Response> resolveAndSuspend(
            @Valid @RequestBody ReportStatsDTO.ResolveAndSuspendRequest req) {
        return ResponseEntity.ok(reportService.resolveAndSuspendUser(req));
    }

    /** Delete a report permanently — admin only. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reportService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
