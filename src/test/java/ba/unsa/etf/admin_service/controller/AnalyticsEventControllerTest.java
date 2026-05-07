package ba.unsa.etf.admin_service.controller;

import ba.unsa.etf.admin_service.dto.AnalyticsEventDTO;
import ba.unsa.etf.admin_service.dto.AnalyticsStatsDTO;
import ba.unsa.etf.admin_service.dto.PagedResponseDTO;
import ba.unsa.etf.admin_service.exception.ResourceNotFoundException;
import ba.unsa.etf.admin_service.model.EventType;
import ba.unsa.etf.admin_service.service.AnalyticsEventService;
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

@WebMvcTest(AnalyticsEventController.class)
class AnalyticsEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsEventService analyticsEventService;

    private AnalyticsEventDTO.Response sampleResponse() {
        return AnalyticsEventDTO.Response.builder()
                .id(1L)
                .userId(1L)
                .eventType(EventType.POST_VIEW)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ✅ POST /api/analytics
    @Test
    void create_success() throws Exception {
        when(analyticsEventService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/analytics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": 1, "eventType": "POST_VIEW"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.eventType").value("POST_VIEW"));
    }

    // ❌ POST /api/analytics — validation fail (bez eventType)
    @Test
    void create_missingEventType_returns400() throws Exception {
        mockMvc.perform(post("/api/analytics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"));
    }

    // ✅ GET /api/analytics
    @Test
    void getAll_success() throws Exception {
        when(analyticsEventService.getAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ✅ GET /api/analytics?eventType=POST_VIEW
    @Test
    void getAll_withEventType() throws Exception {
        when(analyticsEventService.getByEventType(EventType.POST_VIEW))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/analytics?eventType=POST_VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(analyticsEventService).getByEventType(EventType.POST_VIEW);
        verify(analyticsEventService, never()).getAll();
    }

    // ✅ GET /api/analytics/paged
    @Test
    void getAllPaged_success() throws Exception {
        PagedResponseDTO<AnalyticsEventDTO.Response> paged = PagedResponseDTO
                .<AnalyticsEventDTO.Response>builder()
                .content(List.of(sampleResponse()))
                .page(0).size(10).totalElements(1).totalPages(1).last(true)
                .build();
        when(analyticsEventService.getAllPaged(anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/analytics/paged?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ✅ GET /api/analytics/{id}
    @Test
    void getById_success() throws Exception {
        when(analyticsEventService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/analytics/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ❌ GET /api/analytics/{id} — nije pronađen
    @Test
    void getById_notFound() throws Exception {
        when(analyticsEventService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Event sa ID 99 nije pronađen"));

        mockMvc.perform(get("/api/analytics/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    // ✅ GET /api/analytics/stats
    @Test
    void getStats_success() throws Exception {
        AnalyticsStatsDTO.EventTypeCount stats = AnalyticsStatsDTO.EventTypeCount.builder()
                .countByEventType(Map.of("POST_VIEW", 5L))
                .totalEvents(5L)
                .build();
        when(analyticsEventService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/analytics/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").value(5));
    }

    // ✅ POST /api/analytics/batch
    @Test
    void batchCreate_success() throws Exception {
        AnalyticsStatsDTO.BatchResponse response = AnalyticsStatsDTO.BatchResponse.builder()
                .created(2).results(List.of(sampleResponse(), sampleResponse())).build();
        when(analyticsEventService.batchCreate(any())).thenReturn(response);

        mockMvc.perform(post("/api/analytics/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "events": [
                                        {"userId": 1, "eventType": "POST_VIEW"},
                                        {"userId": 2, "eventType": "POST_VIEW"}
                                    ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.created").value(2));
    }

    // ❌ POST /batch — validation fail (prazna lista)
    @Test
    void batchCreate_emptyList_returns400() throws Exception {
        mockMvc.perform(post("/api/analytics/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"events": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"));
    }

    // ✅ DELETE /api/analytics/{id}
    @Test
    void delete_success() throws Exception {
        doNothing().when(analyticsEventService).delete(1L);

        mockMvc.perform(delete("/api/analytics/1"))
                .andExpect(status().isNoContent());
    }

    // ❌ DELETE — nije pronađen
    @Test
    void delete_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Event sa ID 99 nije pronađen"))
                .when(analyticsEventService).delete(99L);

        mockMvc.perform(delete("/api/analytics/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }
}
