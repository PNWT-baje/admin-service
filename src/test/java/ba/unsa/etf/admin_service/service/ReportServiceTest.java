package ba.unsa.etf.admin_service.service;

import ba.unsa.etf.admin_service.dto.ReportDTO;
import ba.unsa.etf.admin_service.exception.ResourceNotFoundException;
import ba.unsa.etf.admin_service.model.Report;
import ba.unsa.etf.admin_service.model.ReportReason;
import ba.unsa.etf.admin_service.model.ReportStatus;
import ba.unsa.etf.admin_service.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

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
        verify(reportRepository, times(1)).save(any());
    }

    @Test
    void getById_shouldReturnReport_whenExists() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));

        ReportDTO.Response response = reportService.getById(1L);

        assertThat(response.getReporterUserId()).isEqualTo(1L);
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

        List<ReportDTO.Response> result = reportService.getAll();

        assertThat(result).hasSize(1);
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
    }

    @Test
    void delete_shouldCallRepository_whenExists() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));

        reportService.delete(1L);

        verify(reportRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}