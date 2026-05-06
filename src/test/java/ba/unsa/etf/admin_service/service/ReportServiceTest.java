package ba.unsa.etf.admin_service.service;

import ba.unsa.etf.admin_service.dto.*;
import ba.unsa.etf.admin_service.exception.ResourceNotFoundException;
import ba.unsa.etf.admin_service.model.Report;
import ba.unsa.etf.admin_service.model.ReportStatus;
import ba.unsa.etf.admin_service.model.ReportReason;
import ba.unsa.etf.admin_service.model.UserSuspension;
import ba.unsa.etf.admin_service.repository.ReportRepository;
import ba.unsa.etf.admin_service.repository.UserSuspensionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserSuspensionRepository suspensionRepository;

    @InjectMocks
    private ReportService reportService;

    private Report sampleReport;

    @BeforeEach
    void setUp() {
        sampleReport = Report.builder()
                .id(1L)
                .reporterUserId(1L)
                .reportedUserId(2L)
                .reason(ReportReason.SPAM)
                .status(ReportStatus.PENDING)
                .build();
    }

    // ─── BASIC CRUD ───────────────────────────────────────────────────────────

    @Test
    void create_shouldReturnResponse() {
        when(reportRepository.save(any())).thenReturn(sampleReport);

        ReportDTO.Request req = new ReportDTO.Request();
        req.setReporterUserId(1L);
        req.setReportedUserId(2L);
        req.setReason(ReportReason.SPAM);

        ReportDTO.Response response = reportService.create(req);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(ReportStatus.PENDING);
        verify(reportRepository).save(any());
    }

    @Test
    void getById_shouldReturnReport_whenExists() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        assertThat(reportService.getById(1L).getReporterUserId()).isEqualTo(1L);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reportService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_shouldReturnList() {
        when(reportRepository.findAll()).thenReturn(List.of(sampleReport));
        assertThat(reportService.getAll()).hasSize(1);
    }

    @Test
    void updateStatus_shouldChangeStatus() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReportDTO.StatusUpdate req = new ReportDTO.StatusUpdate();
        req.setStatus(ReportStatus.RESOLVED);
        req.setReviewedByUserId(99L);

        ReportDTO.Response response = reportService.updateStatus(1L, req);

        assertThat(response.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(response.getReviewedByUserId()).isEqualTo(99L);
        assertThat(response.getResolvedAt()).isNotNull();
    }

    @Test
    void updateStatus_shouldSetResolvedAt_whenDismissed() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReportDTO.StatusUpdate req = new ReportDTO.StatusUpdate();
        req.setStatus(ReportStatus.DISMISSED);

        assertThat(reportService.updateStatus(1L, req).getResolvedAt()).isNotNull();
    }

    @Test
    void delete_shouldCallRepository() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        reportService.delete(1L);
        verify(reportRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reportService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── PAGINACIJA ───────────────────────────────────────────────────────────

    @Test
    void getAllPaged_noFilter_shouldReturnPage() {
        Page<Report> mockPage = new PageImpl<>(List.of(sampleReport),
                PageRequest.of(0, 10, Sort.by("createdAt").descending()), 1);
        when(reportRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        PagedResponseDTO<ReportDTO.Response> result =
                reportService.getAllPaged(0, 10, "createdAt", "desc", null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPage()).isEqualTo(0);
    }

    @Test
    void getAllPaged_withStatusFilter_shouldCallFindByStatus() {
        Page<Report> mockPage = new PageImpl<>(List.of(sampleReport));
        when(reportRepository.findByStatus(eq(ReportStatus.PENDING), any(Pageable.class)))
                .thenReturn(mockPage);

        reportService.getAllPaged(0, 10, "createdAt", "asc", ReportStatus.PENDING, null);

        verify(reportRepository).findByStatus(eq(ReportStatus.PENDING), any(Pageable.class));
        verify(reportRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getAllPaged_withReasonFilter_shouldCallFindByReason() {
        Page<Report> mockPage = new PageImpl<>(List.of(sampleReport));
        when(reportRepository.findByReason(eq(ReportReason.SPAM), any(Pageable.class)))
                .thenReturn(mockPage);

        reportService.getAllPaged(0, 10, "createdAt", "asc", null, ReportReason.SPAM);

        verify(reportRepository).findByReason(eq(ReportReason.SPAM), any(Pageable.class));
    }

    @Test
    void getAllPaged_shouldReturnEmptyPage_whenNoResults() {
        when(reportRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        PagedResponseDTO<ReportDTO.Response> result =
                reportService.getAllPaged(0, 10, "createdAt", "desc", null, null);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void getAllPaged_shouldSortDescending_whenDirectionDesc() {
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(reportRepository.findAll(pageableCaptor.capture())).thenReturn(Page.empty());

        reportService.getAllPaged(0, 5, "createdAt", "desc", null, null);

        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    // ─── PATCH ────────────────────────────────────────────────────────────────

    @Test
    void patch_shouldUpdateDescription() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReportDTO.Response response = reportService.patch(1L, Map.of("description", "Nova deskripcija"));

        assertThat(response.getDescription()).isEqualTo("Nova deskripcija");
    }

    @Test
    void patch_shouldUpdateStatus() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReportDTO.Response response = reportService.patch(1L, Map.of("status", "REVIEWED"));

        assertThat(response.getStatus()).isEqualTo(ReportStatus.REVIEWED);
    }

    @Test
    void patch_shouldSetResolvedAt_whenStatusResolved() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReportDTO.Response response = reportService.patch(1L, Map.of("status", "RESOLVED"));

        assertThat(response.getResolvedAt()).isNotNull();
    }

    @Test
    void patch_shouldUpdateReviewedByUserId() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReportDTO.Response response = reportService.patch(1L, Map.of("reviewedByUserId", 99));

        assertThat(response.getReviewedByUserId()).isEqualTo(99L);
    }

    @Test
    void patch_shouldThrow_whenNotFound() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.patch(99L, Map.of("description", "test")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void patch_shouldIgnoreUnknownFields() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(() ->
                reportService.patch(1L, Map.of("nepoznatoPolje", "vrijednost")));
    }

    // ─── BATCH UNOS ───────────────────────────────────────────────────────────

    @Test
    void batchCreate_shouldCreateAllReports() {
        when(reportRepository.save(any())).thenReturn(sampleReport);

        ReportDTO.Request req1 = new ReportDTO.Request();
        req1.setReporterUserId(1L);
        req1.setReason(ReportReason.SPAM);

        ReportDTO.Request req2 = new ReportDTO.Request();
        req2.setReporterUserId(2L);
        req2.setReason(ReportReason.HARASSMENT);

        ReportStatsDTO.BatchRequest batchReq = new ReportStatsDTO.BatchRequest();
        batchReq.setReports(List.of(req1, req2));

        ReportStatsDTO.BatchResponse response = reportService.batchCreate(batchReq);

        assertThat(response.getCreated()).isEqualTo(2);
        assertThat(response.getFailed()).isEqualTo(0);
        assertThat(response.getResults()).hasSize(2);
        verify(reportRepository, times(2)).save(any());
    }

    @Test
    void batchCreate_shouldHandlePartialFailure() {
        when(reportRepository.save(any()))
                .thenReturn(sampleReport)
                .thenThrow(new RuntimeException("DB greška"));

        ReportDTO.Request req1 = new ReportDTO.Request();
        req1.setReporterUserId(1L);
        req1.setReason(ReportReason.SPAM);

        ReportDTO.Request req2 = new ReportDTO.Request();
        req2.setReporterUserId(2L);
        req2.setReason(ReportReason.HARASSMENT);

        ReportStatsDTO.BatchRequest batchReq = new ReportStatsDTO.BatchRequest();
        batchReq.setReports(List.of(req1, req2));

        ReportStatsDTO.BatchResponse response = reportService.batchCreate(batchReq);

        assertThat(response.getCreated()).isEqualTo(1);
        assertThat(response.getFailed()).isEqualTo(1);
    }

    @Test
    void batchCreate_shouldReturnEmpty_whenListIsEmpty() {
        ReportStatsDTO.BatchRequest batchReq = new ReportStatsDTO.BatchRequest();
        batchReq.setReports(List.of());

        ReportStatsDTO.BatchResponse response = reportService.batchCreate(batchReq);

        assertThat(response.getCreated()).isEqualTo(0);
        assertThat(response.getFailed()).isEqualTo(0);
        verifyNoInteractions(reportRepository);
    }

    // ─── TRANSAKCIJA ─────────────────────────────────────────────────────────

    @Test
    void resolveAndSuspend_shouldResolveReportAndSuspendUser() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(suspensionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReportStatsDTO.ResolveAndSuspendRequest req = new ReportStatsDTO.ResolveAndSuspendRequest();
        req.setReportId(1L);
        req.setAdminUserId(99L);
        req.setSuspensionReason("Kršenje pravila");
        req.setSuspendedUntil(LocalDateTime.now().plusDays(7));

        ReportDTO.Response response = reportService.resolveAndSuspendUser(req);

        assertThat(response.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(response.getReviewedByUserId()).isEqualTo(99L);
        assertThat(response.getResolvedAt()).isNotNull();

        ArgumentCaptor<UserSuspension> captor = ArgumentCaptor.forClass(UserSuspension.class);
        verify(suspensionRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(2L);
        assertThat(captor.getValue().getSuspendedByUserId()).isEqualTo(99L);
        assertThat(captor.getValue().getReason()).isEqualTo("Kršenje pravila");
    }

    @Test
    void resolveAndSuspend_shouldNotSuspend_whenNoReportedUser() {
        sampleReport.setReportedUserId(null);
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReportStatsDTO.ResolveAndSuspendRequest req = new ReportStatsDTO.ResolveAndSuspendRequest();
        req.setReportId(1L);
        req.setAdminUserId(99L);

        reportService.resolveAndSuspendUser(req);

        verifyNoInteractions(suspensionRepository);
    }

    @Test
    void resolveAndSuspend_shouldUseDefaultReason_whenNull() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(suspensionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReportStatsDTO.ResolveAndSuspendRequest req = new ReportStatsDTO.ResolveAndSuspendRequest();
        req.setReportId(1L);
        req.setAdminUserId(99L);
        req.setSuspensionReason(null);

        reportService.resolveAndSuspendUser(req);

        ArgumentCaptor<UserSuspension> captor = ArgumentCaptor.forClass(UserSuspension.class);
        verify(suspensionRepository).save(captor.capture());
        assertThat(captor.getValue().getReason()).contains("Automatska suspenzija");
    }

    @Test
    void resolveAndSuspend_shouldThrow_whenReportNotFound() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());

        ReportStatsDTO.ResolveAndSuspendRequest req = new ReportStatsDTO.ResolveAndSuspendRequest();
        req.setReportId(99L);
        req.setAdminUserId(1L);

        assertThatThrownBy(() -> reportService.resolveAndSuspendUser(req))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(suspensionRepository);
    }

    // ─── CUSTOM UPITI ─────────────────────────────────────────────────────────

    @Test
    void getStats_shouldReturnCorrectCounts() {
        when(reportRepository.countByStatus()).thenReturn(List.of(
                new Object[]{"PENDING", 3L},
                new Object[]{"RESOLVED", 2L}
        ));

        ReportStatsDTO.StatusCount stats = reportService.getStats();

        assertThat(stats.getTotalReports()).isEqualTo(5);
        assertThat(stats.getPendingReports()).isEqualTo(3);
        assertThat(stats.getCountByStatus()).containsEntry("PENDING", 3L);
        assertThat(stats.getCountByStatus()).containsEntry("RESOLVED", 2L);
    }

    @Test
    void getStats_shouldReturnZero_whenNoReports() {
        when(reportRepository.countByStatus()).thenReturn(List.of());

        ReportStatsDTO.StatusCount stats = reportService.getStats();

        assertThat(stats.getTotalReports()).isEqualTo(0);
        assertThat(stats.getPendingReports()).isEqualTo(0);
    }

    @Test
    void getInvolvingUser_shouldReturnReports() {
        when(reportRepository.findAllInvolvingUser(1L)).thenReturn(List.of(sampleReport));

        List<ReportDTO.Response> result = reportService.getInvolvingUser(1L);

        assertThat(result).hasSize(1);
        verify(reportRepository).findAllInvolvingUser(1L);
    }

    @Test
    void getOldPendingReports_shouldCallRepositoryWithCorrectThreshold() {
        when(reportRepository.findOldPendingReports(any(LocalDateTime.class)))
                .thenReturn(List.of(sampleReport));

        List<ReportDTO.Response> result = reportService.getOldPendingReports(7);

        assertThat(result).hasSize(1);
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(reportRepository).findOldPendingReports(captor.capture());
        assertThat(captor.getValue()).isBefore(LocalDateTime.now().minusDays(6));
    }
}