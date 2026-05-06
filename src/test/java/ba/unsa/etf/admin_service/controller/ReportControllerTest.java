package ba.unsa.etf.admin_service.controller;

import ba.unsa.etf.admin_service.dto.ReportDTO;
import ba.unsa.etf.admin_service.exception.ResourceNotFoundException;
import ba.unsa.etf.admin_service.model.ReportReason;
import ba.unsa.etf.admin_service.model.ReportStatus;
import ba.unsa.etf.admin_service.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

    @Test
    void createReport_success() throws Exception {
        when(reportService.create(any())).thenReturn(sampleResponse());

        String body = """
                {
                    "reporterUserId": 1,
                    "reportedUserId": 2,
                    "reason": "SPAM",
                    "description": "Testni opis"
                }
                """;

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createReport_validationFail_missingReporterUserId() throws Exception {
        String body = """
                {
                    "reason": "SPAM"
                }
                """;

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void createReport_invalidEnum() throws Exception {
        String body = """
                {
                    "reporterUserId": 1,
                    "reason": "NEPOSTOJECI_ENUM"
                }
                """;

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"));
    }

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

    @Test
    void getAll_success() throws Exception {
        when(reportService.getAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}