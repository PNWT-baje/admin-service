package ba.unsa.etf.admin_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ba.unsa.etf.admin_service.model.Report;
import ba.unsa.etf.admin_service.model.ReportStatus;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByStatus(ReportStatus status);

    List<Report> findByReportedUserId(Long reportedUserId);

    List<Report> findByReporterUserId(Long reporterUserId);

    List<Report> findByReportedPostId(Long reportedPostId);

    List<Report> findByReviewedByUserId(Long adminUserId);
}