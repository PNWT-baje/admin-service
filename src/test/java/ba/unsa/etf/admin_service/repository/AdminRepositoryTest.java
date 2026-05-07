package ba.unsa.etf.admin_service.repository;

import ba.unsa.etf.admin_service.model.Report;
import ba.unsa.etf.admin_service.model.ReportNote;
import ba.unsa.etf.admin_service.model.ReportReason;
import ba.unsa.etf.admin_service.model.ReportStatus;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AdminRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportNoteRepository noteRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private EntityManager entityManager;

    private Long reportId;

    @BeforeEach
    void setUp() {
        Report report = reportRepository.save(Report.builder()
                .reporterUserId(1L)
                .reportedUserId(2L)
                .reason(ReportReason.SPAM)
                .status(ReportStatus.PENDING)
                .build());

        noteRepository.save(ReportNote.builder()
                .report(report).adminUserId(99L).content("Provjeri profil").build());
        noteRepository.save(ReportNote.builder()
                .report(report).adminUserId(99L).content("Eskalirati").build());

        reportId = report.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByStatus_returnsCorrectReports() {
        List<Report> pending = reportRepository.findByStatus(ReportStatus.PENDING);
        assertEquals(1, pending.size());
        assertEquals(ReportStatus.PENDING, pending.get(0).getStatus());
    }

    @Test
    void findAllInvolvingUser_returnsReportWhenReporter() {
        List<Report> reports = reportRepository.findAllInvolvingUser(1L);
        assertEquals(1, reports.size());
    }

    @Test
    void findByReportedPostId_returnsEmpty() {
        List<Report> reports = reportRepository.findByReportedPostId(999L);
        assertTrue(reports.isEmpty());
    }

    @Test
    void entityGraph_reducesQueryCountForNotes() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();

        // Bez EntityGraph-a: report + notes = 2 query-a
        statistics.clear();
        Report lazyReport = reportRepository.findById(reportId).orElseThrow();
        lazyReport.getNotes().size();
        long withoutGraph = statistics.getPrepareStatementCount();

        entityManager.clear();

        // Sa EntityGraph-om: sve u jednom JOIN query-u
        statistics.clear();
        Report eagerReport = reportRepository.findWithNotesById(reportId).orElseThrow();
        if (eagerReport.getNotes() != null) eagerReport.getNotes().size();
        long withGraph = statistics.getPrepareStatementCount();

        assertTrue(withGraph <= withoutGraph,
                "EntityGraph trebao smanjiti broj upita: bez=" + withoutGraph + " sa=" + withGraph);
    }

    @Test
    void findWithNotesById_loadsNotesEagerly() {
        Report report = reportRepository.findWithNotesById(reportId).orElseThrow();
        assertNotNull(report.getNotes());
        assertEquals(2, report.getNotes().size());
    }

    @Test
    void noteRepository_findByReportId_returnsCorrect() {
        List<ReportNote> notes = noteRepository.findByReportIdOrderByCreatedAtDesc(reportId);
        assertEquals(2, notes.size());
    }
}
