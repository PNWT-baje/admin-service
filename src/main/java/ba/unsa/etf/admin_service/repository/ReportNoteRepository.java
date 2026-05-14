package ba.unsa.etf.admin_service.repository;

import ba.unsa.etf.admin_service.model.ReportNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportNoteRepository extends JpaRepository<ReportNote, Long> {
    List<ReportNote> findByReportIdOrderByCreatedAtDesc(Long reportId);
}
