package ba.unsa.etf.admin_service.repository;

import ba.unsa.etf.admin_service.model.Report;
import ba.unsa.etf.admin_service.model.ReportReason;
import ba.unsa.etf.admin_service.model.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // Paginacija + filter po statusu
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    // Svi reports sa paginacijom
    Page<Report> findAll(Pageable pageable);

    // Custom: broj reportova po statusu
    @Query("SELECT r.status, COUNT(r) FROM Report r GROUP BY r.status")
    List<Object[]> countByStatus();

    // Custom: reports za određenog korisnika (i kao reporter i kao reported)
    @Query("SELECT r FROM Report r WHERE r.reporterUserId = :userId OR r.reportedUserId = :userId")
    List<Report> findAllInvolvingUser(@Param("userId") Long userId);

    // Custom: neriješeni reports stariji od X dana
    @Query("SELECT r FROM Report r WHERE r.status = 'PENDING' AND r.createdAt < :threshold")
    List<Report> findOldPendingReports(@Param("threshold") LocalDateTime threshold);

    // Custom: reports po razlogu sa paginacijom
    Page<Report> findByReason(ReportReason reason, Pageable pageable);

    List<Report> findByStatus(ReportStatus status);

    List<Report> findByReportedUserId(Long reportedUserId);

    List<Report> findByReporterUserId(Long reporterUserId);

    @Query("SELECT r FROM Report r WHERE r.reportedPostId = :postId")
    List<Report> findByReportedPostId(@Param("postId") Long postId);

    List<Report> findByReviewedByUserId(Long adminUserId);

    // EntityGraph — Task 4: ucitava notes u jednom JOIN query-u (N+1 fix)
    @EntityGraph(attributePaths = {"notes"})
    Optional<Report> findWithNotesById(Long id);
}