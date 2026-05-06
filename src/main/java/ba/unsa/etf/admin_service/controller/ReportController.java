package ba.unsa.etf.admin_service.controller;

import ba.unsa.etf.admin_service.dto.ReportDTO;
import ba.unsa.etf.admin_service.model.ReportStatus;
import ba.unsa.etf.admin_service.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // GET /api/reports
    @GetMapping
    public ResponseEntity<List<ReportDTO.Response>> getAll(
            @RequestParam(required = false) ReportStatus status) {
        if (status != null) {
            return ResponseEntity.ok(reportService.getByStatus(status));
        }
        return ResponseEntity.ok(reportService.getAll());
    }

    // GET /api/reports/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ReportDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getById(id));
    }

    // PATCH /api/reports/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReportDTO.Response> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReportDTO.StatusUpdate req) {
        return ResponseEntity.ok(reportService.updateStatus(id, req));
    }

    // DELETE /api/reports/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reportService.delete(id);
        return ResponseEntity.noContent().build();
    }
}