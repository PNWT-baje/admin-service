package ba.unsa.etf.admin_service.controller;

import ba.unsa.etf.admin_service.dto.*;
import ba.unsa.etf.admin_service.exception.ResourceNotFoundException;
import ba.unsa.etf.admin_service.model.ReportStatus;
import ba.unsa.etf.admin_service.model.ReportReason;
import ba.unsa.etf.admin_service.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReportService reportService;

    private ReportDTO.Response sampleResponse() {
        return ReportDTO.Response.builder()
                .id(1L)
                .reporterUserId(1L)
                .reportedUserId(2L)
                .reason(ReportReason.SPAM)
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ─── POST /api/reports ────────────────────────────────────────────────────

    @Test
    void createReport_success() throws Exception {
        when(reportService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "reporterUserId": 1,
                                    "reportedUserId": 2,
                                    "reason": "SPAM",
                                    "description": "Testna prijava"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createReport_validationFail_missingReporterUserId() throws Exception {
        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "reason": "SPAM"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void createReport_validationFail_missingReason() throws Exception {
        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "reporterUserId": 1,
                                    "description": "Bez razloga"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"));
    }

    @Test
    void createReport_invalidEnum_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "reporterUserId": 1,
                                    "reason": "NEPOSTOJECI"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"));
    }

    // ─── GET /api/reports ─────────────────────────────────────────────────────

    @Test
    void getAll_success() throws Exception {
        when(reportService.getAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAll_withStatusFilter() throws Exception {
        when(reportService.getByStatus(ReportStatus.PENDING))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/reports?status=PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(reportService).getByStatus(ReportStatus.PENDING);
        verify(reportService, never()).getAll();
    }

    // ─── GET /api/reports/paged ───────────────────────────────────────────────

    @Test
    void getAllPaged_success() throws Exception {
        PagedResponseDTO<ReportDTO.Response> paged = PagedResponseDTO.<ReportDTO.Response>builder()
                .content(List.of(sampleResponse()))
                .page(0).size(10).totalElements(1).totalPages(1).last(true)
                .build();
        when(reportService.getAllPaged(anyInt(), anyInt(), anyString(), anyString(), any(), any()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/reports/paged?page=0&size=10&sortBy=createdAt&direction=desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void getAllPaged_withStatusFilter() throws Exception {
        PagedResponseDTO<ReportDTO.Response> paged = PagedResponseDTO.<ReportDTO.Response>builder()
                .content(List.of(sampleResponse()))
                .page(0).size(10).totalElements(1).totalPages(1).last(true)
                .build();
        when(reportService.getAllPaged(eq(0), eq(10), anyString(), anyString(),
                eq(ReportStatus.PENDING), isNull()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/reports/paged?status=PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ─── GET /api/reports/{id} ────────────────────────────────────────────────

    @Test
    void getById_success() throws Exception {
        when(reportService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_notFound() throws Exception {
        when(reportService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Report sa ID 99 nije pronađen"));

        mockMvc.perform(get("/api/reports/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"))
                .andExpect(jsonPath("$.message").value("Report sa ID 99 nije pronađen"));
    }

    // ─── GET /api/reports/user/{userId}/involved ──────────────────────────────

    @Test
    void getInvolvingUser_success() throws Exception {
        when(reportService.getInvolvingUser(1L)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/reports/user/1/involved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ─── GET /api/reports/pending/old ─────────────────────────────────────────

    @Test
    void getOldPending_success() throws Exception {
        when(reportService.getOldPendingReports(7)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/reports/pending/old?days=7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getOldPending_defaultDays() throws Exception {
        when(reportService.getOldPendingReports(7)).thenReturn(List.of());

        mockMvc.perform(get("/api/reports/pending/old"))
                .andExpect(status().isOk());

        verify(reportService).getOldPendingReports(7);
    }

    // ─── GET /api/reports/stats ───────────────────────────────────────────────

    @Test
    void getStats_success() throws Exception {
        ReportStatsDTO.StatusCount stats = ReportStatsDTO.StatusCount.builder()
                .countByStatus(Map.of("PENDING", 3L, "RESOLVED", 2L))
                .totalReports(5L)
                .pendingReports(3L)
                .build();
        when(reportService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/reports/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReports").value(5))
                .andExpect(jsonPath("$.pendingReports").value(3));
    }

    // ─── PATCH /api/reports/{id}/status ──────────────────────────────────────

    @Test
    void updateStatus_success() throws Exception {
        ReportDTO.Response resolved = sampleResponse();
        resolved.setStatus(ReportStatus.RESOLVED);
        when(reportService.updateStatus(eq(1L), any())).thenReturn(resolved);

        mockMvc.perform(patch("/api/reports/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "RESOLVED",
                                    "reviewedByUserId": 99
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    void updateStatus_validationFail_missingStatus() throws Exception {
        mockMvc.perform(patch("/api/reports/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"));
    }

    @Test
    void updateStatus_notFound() throws Exception {
        when(reportService.updateStatus(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Report sa ID 99 nije pronađen"));

        mockMvc.perform(patch("/api/reports/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "RESOLVED"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    // ─── PATCH /api/reports/{id} ──────────────────────────────────────────────

    @Test
    void patch_success() throws Exception {
        ReportDTO.Response patched = sampleResponse();
        patched.setDescription("Novi opis");
        when(reportService.patch(eq(1L), any())).thenReturn(patched);

        mockMvc.perform(patch("/api/reports/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "Novi opis"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Novi opis"));
    }

    @Test
    void patch_notFound() throws Exception {
        when(reportService.patch(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Report sa ID 99 nije pronađen"));

        mockMvc.perform(patch("/api/reports/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "test"}
                                """))
                .andExpect(status().isNotFound());
    }

    // ─── POST /api/reports/batch ──────────────────────────────────────────────

    @Test
    void batchCreate_success() throws Exception {
        ReportStatsDTO.BatchResponse batchResponse = ReportStatsDTO.BatchResponse.builder()
                .created(2).failed(0).results(List.of(sampleResponse(), sampleResponse()))
                .build();
        when(reportService.batchCreate(any())).thenReturn(batchResponse);

        mockMvc.perform(post("/api/reports/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "reports": [
                                        {"reporterUserId": 1, "reason": "SPAM"},
                                        {"reporterUserId": 2, "reason": "HARASSMENT"}
                                    ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.created").value(2))
                .andExpect(jsonPath("$.failed").value(0));
    }

    @Test
    void batchCreate_validationFail_emptyList() throws Exception {
        mockMvc.perform(post("/api/reports/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reports": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"));
    }

    // ─── POST /api/reports/resolve-and-suspend ────────────────────────────────

    @Test
    void resolveAndSuspend_success() throws Exception {
        ReportDTO.Response resolved = sampleResponse();
        resolved.setStatus(ReportStatus.RESOLVED);
        when(reportService.resolveAndSuspendUser(any())).thenReturn(resolved);

        mockMvc.perform(post("/api/reports/resolve-and-suspend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "reportId": 1,
                                    "adminUserId": 99,
                                    "suspensionReason": "Kršenje pravila"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    void resolveAndSuspend_validationFail_missingReportId() throws Exception {
        mockMvc.perform(post("/api/reports/resolve-and-suspend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"adminUserId": 99}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"));
    }

    @Test
    void resolveAndSuspend_notFound() throws Exception {
        when(reportService.resolveAndSuspendUser(any()))
                .thenThrow(new ResourceNotFoundException("Report sa ID 99 nije pronađen"));

        mockMvc.perform(post("/api/reports/resolve-and-suspend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reportId": 99, "adminUserId": 1}
                                """))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /api/reports/{id} ─────────────────────────────────────────────

    @Test
    void delete_success() throws Exception {
        doNothing().when(reportService).delete(1L);

        mockMvc.perform(delete("/api/reports/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Report sa ID 99 nije pronađen"))
                .when(reportService).delete(99L);

        mockMvc.perform(delete("/api/reports/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }
}