package ba.unsa.etf.admin_service.service;

import ba.unsa.etf.admin_service.dto.ReportDTO;
import ba.unsa.etf.admin_service.exception.ResourceNotFoundException;
import ba.unsa.etf.admin_service.model.Report;
import ba.unsa.etf.admin_service.model.ReportStatus;
import ba.unsa.etf.admin_service.model.Report;
import ba.unsa.etf.admin_service.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

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

    private Report findOrThrow(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report sa ID " + id + " nije pronađen"));
    }

    private ReportDTO.Response toResponse(Report r) {
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