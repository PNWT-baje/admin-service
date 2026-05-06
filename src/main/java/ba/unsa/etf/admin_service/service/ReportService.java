package ba.unsa.etf.admin_service.service;

import ba.unsa.etf.admin_service.dto.*;
import ba.unsa.etf.admin_service.exception.ResourceNotFoundException;
import ba.unsa.etf.admin_service.model.Report;
import ba.unsa.etf.admin_service.model.ReportReason;
import ba.unsa.etf.admin_service.model.ReportStatus;
import ba.unsa.etf.admin_service.model.UserSuspension;
import ba.unsa.etf.admin_service.repository.ReportRepository;
import ba.unsa.etf.admin_service.repository.UserSuspensionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserSuspensionRepository suspensionRepository;

    // ─── BASIC CRUD ───────────────────────────────────────────────────────────

    public ReportDTO.Response create(ReportDTO.Request req) {
        Report report = Report.builder()
                .reporterUserId(req.getReporterUserId())
                .reportedUserId(req.getReportedUserId())
                .reportedPostId(req.getReportedPostId())
                .reportedCommentId(req.getReportedCommentId())
                .reason(req.getReason())
                .description(req.getDescription())
                .status(ReportStatus.PENDING)
                .build();
        return toResponse(reportRepository.save(report));
    }

    public List<ReportDTO.Response> getAll() {
        return reportRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ReportDTO.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<ReportDTO.Response> getByStatus(ReportStatus status) {
        return reportRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ReportDTO.Response updateStatus(Long id, ReportDTO.StatusUpdate req) {
        Report report = findOrThrow(id);
        report.setStatus(req.getStatus());
        report.setReviewedByUserId(req.getReviewedByUserId());
        if (req.getStatus() == ReportStatus.RESOLVED ||
                req.getStatus() == ReportStatus.DISMISSED) {
            report.setResolvedAt(LocalDateTime.now());
        }
        return toResponse(reportRepository.save(report));
    }

    public void delete(Long id) {
        findOrThrow(id);
        reportRepository.deleteById(id);
    }

    // ─── 1. PAGINACIJA I SORTIRANJE ───────────────────────────────────────────

    public PagedResponseDTO<ReportDTO.Response> getAllPaged(
            int page, int size, String sortBy, String direction,
            ReportStatus status, ReportReason reason) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Report> result;
        if (status != null) {
            result = reportRepository.findByStatus(status, pageable);
        } else if (reason != null) {
            result = reportRepository.findByReason(reason, pageable);
        } else {
            result = reportRepository.findAll(pageable);
        }

        return PagedResponseDTO.from(result.map(this::toResponse));
    }

    // ─── 2. PATCH METODA ─────────────────────────────────────────────────────

    public ReportDTO.Response patch(Long id, Map<String, Object> fields) {
        Report report = findOrThrow(id);

        fields.forEach((key, value) -> {
            switch (key) {
                case "description" -> report.setDescription((String) value);
                case "status" -> {
                    ReportStatus newStatus = ReportStatus.valueOf((String) value);
                    report.setStatus(newStatus);
                    if (newStatus == ReportStatus.RESOLVED ||
                            newStatus == ReportStatus.DISMISSED) {
                        report.setResolvedAt(LocalDateTime.now());
                    }
                }
                case "reviewedByUserId" -> report.setReviewedByUserId(((Number) value).longValue());
                default -> log.warn("Nepoznato polje za patch: {}", key);
            }
        });

        return toResponse(reportRepository.save(report));
    }

    // ─── 3. BATCH UNOS ────────────────────────────────────────────────────────

    @Transactional
    public ReportStatsDTO.BatchResponse batchCreate(ReportStatsDTO.BatchRequest req) {
        List<ReportDTO.Response> results = new ArrayList<>();
        int failed = 0;

        for (ReportDTO.Request r : req.getReports()) {
            try {
                results.add(create(r));
            } catch (Exception e) {
                log.error("Greška pri batch kreiranju reporta: {}", e.getMessage());
                failed++;
            }
        }

        return ReportStatsDTO.BatchResponse.builder()
                .created(results.size())
                .failed(failed)
                .results(results)
                .build();
    }

    // ─── 4. TRANSAKCIJSKA METODA: resolve + suspend user ─────────────────────

    @Transactional
    public ReportDTO.Response resolveAndSuspendUser(ReportStatsDTO.ResolveAndSuspendRequest req) {
        Report report = findOrThrow(req.getReportId());
        report.setStatus(ReportStatus.RESOLVED);
        report.setReviewedByUserId(req.getAdminUserId());
        report.setResolvedAt(LocalDateTime.now());
        Report savedReport = reportRepository.save(report);

        if (report.getReportedUserId() != null) {
            UserSuspension suspension = UserSuspension.builder()
                    .userId(report.getReportedUserId())
                    .suspendedByUserId(req.getAdminUserId())
                    .reason(req.getSuspensionReason() != null
                            ? req.getSuspensionReason()
                            : "Automatska suspenzija — razriješena prijava #" + report.getId())
                    .suspendedUntil(req.getSuspendedUntil())
                    .build();
            suspensionRepository.save(suspension);
            log.info("Korisnik {} suspendovan nakon razrješavanja reporta {}",
                    report.getReportedUserId(), report.getId());
        }

        return toResponse(savedReport);
    }

    // ─── 5. CUSTOM UPITI ─────────────────────────────────────────────────────

    public ReportStatsDTO.StatusCount getStats() {
        List<Object[]> rows = reportRepository.countByStatus();
        Map<String, Long> countMap = new HashMap<>();
        long total = 0;
        long pending = 0;

        for (Object[] row : rows) {
            String status = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            countMap.put(status, count);
            total += count;
            if (status.equals("PENDING")) pending = count;
        }

        return ReportStatsDTO.StatusCount.builder()
                .countByStatus(countMap)
                .totalReports(total)
                .pendingReports(pending)
                .build();
    }

    public List<ReportDTO.Response> getInvolvingUser(Long userId) {
        return reportRepository.findAllInvolvingUser(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ReportDTO.Response> getOldPendingReports(int olderThanDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(olderThanDays);
        return reportRepository.findOldPendingReports(threshold).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────

    private Report findOrThrow(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report sa ID " + id + " nije pronađen"));
    }

    public ReportDTO.Response toResponse(Report r) {
        return ReportDTO.Response.builder()
                .id(r.getId())
                .reporterUserId(r.getReporterUserId())
                .reportedUserId(r.getReportedUserId())
                .reportedPostId(r.getReportedPostId())
                .reportedCommentId(r.getReportedCommentId())
                .reason(r.getReason())
                .description(r.getDescription())
                .status(r.getStatus())
                .reviewedByUserId(r.getReviewedByUserId())
                .createdAt(r.getCreatedAt())
                .resolvedAt(r.getResolvedAt())
                .build();
    }
}