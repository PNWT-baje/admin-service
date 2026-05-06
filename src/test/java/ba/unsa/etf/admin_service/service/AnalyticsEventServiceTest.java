package ba.unsa.etf.admin_service.service;

import ba.unsa.etf.admin_service.dto.*;
import ba.unsa.etf.admin_service.exception.ResourceNotFoundException;
import ba.unsa.etf.admin_service.model.AnalyticsEvent;
import ba.unsa.etf.admin_service.model.EventType;
import ba.unsa.etf.admin_service.repository.AnalyticsEventRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventServiceTest {

    @Mock
    private AnalyticsEventRepository analyticsEventRepository;

    @InjectMocks
    private AnalyticsEventService analyticsEventService;

    private AnalyticsEvent sampleEvent;

    @BeforeEach
    void setUp() {
        sampleEvent = AnalyticsEvent.builder()
                .id(1L)
                .userId(1L)
                .eventType(EventType.POST_VIEW)
                .referenceId(10L)
                .referenceType("POST")
                .metadata("{\"source\": \"feed\"}")
                .build();
    }

    // ─── BASIC CRUD ───────────────────────────────────────────────────────────

    @Test
    void create_shouldReturnResponse() {
        when(analyticsEventRepository.save(any())).thenReturn(sampleEvent);

        AnalyticsEventDTO.Request req = new AnalyticsEventDTO.Request();
        req.setUserId(1L);
        req.setEventType(EventType.POST_VIEW);

        AnalyticsEventDTO.Response response = analyticsEventService.create(req);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEventType()).isEqualTo(EventType.POST_VIEW);
        verify(analyticsEventRepository).save(any());
    }

    @Test
    void getById_shouldReturnEvent_whenExists() {
        when(analyticsEventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));

        AnalyticsEventDTO.Response response = analyticsEventService.getById(1L);

        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(analyticsEventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> analyticsEventService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_shouldReturnList() {
        when(analyticsEventRepository.findAll()).thenReturn(List.of(sampleEvent));

        assertThat(analyticsEventService.getAll()).hasSize(1);
    }

    @Test
    void getByUserId_shouldReturnUserEvents() {
        when(analyticsEventRepository.findByUserId(1L)).thenReturn(List.of(sampleEvent));

        List<AnalyticsEventDTO.Response> result = analyticsEventService.getByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void delete_shouldCallRepository() {
        when(analyticsEventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));

        analyticsEventService.delete(1L);

        verify(analyticsEventRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(analyticsEventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> analyticsEventService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── PAGINACIJA ───────────────────────────────────────────────────────────

    @Test
    void getAllPaged_noFilter_shouldReturnPage() {
        Page<AnalyticsEvent> mockPage = new PageImpl<>(List.of(sampleEvent),
                PageRequest.of(0, 10, Sort.by("createdAt").descending()), 1);
        when(analyticsEventRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        PagedResponseDTO<AnalyticsEventDTO.Response> result =
                analyticsEventService.getAllPaged(0, 10, "createdAt", "desc", null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getAllPaged_withEventTypeFilter_shouldCallFindByEventType() {
        Page<AnalyticsEvent> mockPage = new PageImpl<>(List.of(sampleEvent));
        when(analyticsEventRepository.findByEventType(
                eq(EventType.POST_VIEW), any(Pageable.class)))
                .thenReturn(mockPage);

        analyticsEventService.getAllPaged(0, 10, "createdAt", "asc",
                EventType.POST_VIEW);

        verify(analyticsEventRepository).findByEventType(
                eq(EventType.POST_VIEW), any(Pageable.class));
        verify(analyticsEventRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getAllPaged_shouldSortAscending_whenDirectionAsc() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(analyticsEventRepository.findAll(captor.capture())).thenReturn(Page.empty());

        analyticsEventService.getAllPaged(0, 10, "createdAt", "asc", null);

        Sort.Order order = captor.getValue().getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    // ─── BATCH UNOS ───────────────────────────────────────────────────────────

    @Test
    void batchCreate_shouldCreateAllEvents() {
        when(analyticsEventRepository.save(any())).thenReturn(sampleEvent);

        AnalyticsEventDTO.Request req1 = new AnalyticsEventDTO.Request();
        req1.setUserId(1L);
        req1.setEventType(EventType.POST_VIEW);

        AnalyticsEventDTO.Request req2 = new AnalyticsEventDTO.Request();
        req2.setUserId(2L);
        req2.setEventType(EventType.SEARCH);

        AnalyticsStatsDTO.BatchRequest batchReq = new AnalyticsStatsDTO.BatchRequest();
        batchReq.setEvents(List.of(req1, req2));

        AnalyticsStatsDTO.BatchResponse response = analyticsEventService.batchCreate(batchReq);

        assertThat(response.getCreated()).isEqualTo(2);
        assertThat(response.getResults()).hasSize(2);
        verify(analyticsEventRepository, times(2)).save(any());
    }

    @Test
    void batchCreate_shouldReturnEmpty_whenListIsEmpty() {
        AnalyticsStatsDTO.BatchRequest batchReq = new AnalyticsStatsDTO.BatchRequest();
        batchReq.setEvents(List.of());

        AnalyticsStatsDTO.BatchResponse response = analyticsEventService.batchCreate(batchReq);

        assertThat(response.getCreated()).isEqualTo(0);
        verifyNoInteractions(analyticsEventRepository);
    }

    // ─── CUSTOM UPITI ─────────────────────────────────────────────────────────

    @Test
    void getStats_shouldReturnCorrectCounts() {
        when(analyticsEventRepository.countByEventType()).thenReturn(List.of(
                new Object[]{"POST_VIEW", 5L},
                new Object[]{"SEARCH", 3L}
        ));

        AnalyticsStatsDTO.EventTypeCount stats = analyticsEventService.getStats();

        assertThat(stats.getTotalEvents()).isEqualTo(8);
        assertThat(stats.getCountByEventType()).containsEntry("POST_VIEW", 5L);
        assertThat(stats.getCountByEventType()).containsEntry("SEARCH", 3L);
    }

    @Test
    void getStats_shouldReturnZero_whenNoEvents() {
        when(analyticsEventRepository.countByEventType()).thenReturn(List.of());

        AnalyticsStatsDTO.EventTypeCount stats = analyticsEventService.getStats();

        assertThat(stats.getTotalEvents()).isEqualTo(0);
        assertThat(stats.getCountByEventType()).isEmpty();
    }

    @Test
    void getRecentEvents_shouldCallRepositoryWithCorrectTime() {
        when(analyticsEventRepository.findRecentEvents(any(LocalDateTime.class)))
                .thenReturn(List.of(sampleEvent));

        List<AnalyticsEventDTO.Response> result = analyticsEventService.getRecentEvents(24);

        assertThat(result).hasSize(1);
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(analyticsEventRepository).findRecentEvents(captor.capture());
        assertThat(captor.getValue()).isBefore(LocalDateTime.now().minusHours(23));
    }

    @Test
    void getMostActiveUsers_shouldReturnTopN() {
        when(analyticsEventRepository.findMostActiveUsers(any(Pageable.class)))
                .thenReturn(List.of(
                        new Object[]{1L, 10L},
                        new Object[]{2L, 7L}
                ));

        List<AnalyticsStatsDTO.ActiveUser> result = analyticsEventService.getMostActiveUsers(5);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getEventCount()).isEqualTo(10L);
    }
}