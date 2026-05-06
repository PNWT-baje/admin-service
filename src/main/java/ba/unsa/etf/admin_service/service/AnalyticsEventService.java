package ba.unsa.etf.admin_service.service;

import ba.unsa.etf.admin_service.dto.*;
import ba.unsa.etf.admin_service.exception.ResourceNotFoundException;
import ba.unsa.etf.admin_service.model.AnalyticsEvent;
import ba.unsa.etf.admin_service.model.EventType;
import ba.unsa.etf.admin_service.repository.AnalyticsEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsEventService {

    private final AnalyticsEventRepository analyticsEventRepository;

    // ─── BASIC CRUD ───────────────────────────────────────────────────────────

    public AnalyticsEventDTO.Response create(AnalyticsEventDTO.Request req) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .userId(req.getUserId())
                .eventType(req.getEventType())
                .referenceId(req.getReferenceId())
                .referenceType(req.getReferenceType())
                .metadata(req.getMetadata())
                .build();
        return toResponse(analyticsEventRepository.save(event));
    }

    public List<AnalyticsEventDTO.Response> getAll() {
        return analyticsEventRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AnalyticsEventDTO.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<AnalyticsEventDTO.Response> getByUserId(Long userId) {
        return analyticsEventRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AnalyticsEventDTO.Response> getByEventType(EventType eventType) {
        return analyticsEventRepository.findByEventType(eventType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        findOrThrow(id);
        analyticsEventRepository.deleteById(id);
    }

    // ─── 1. PAGINACIJA I SORTIRANJE ───────────────────────────────────────────

    public PagedResponseDTO<AnalyticsEventDTO.Response> getAllPaged(
            int page, int size, String sortBy, String direction,
            EventType eventType) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AnalyticsEvent> result = eventType != null
                ? analyticsEventRepository.findByEventType(eventType, pageable)
                : analyticsEventRepository.findAll(pageable);

        return PagedResponseDTO.from(result.map(this::toResponse));
    }

    // ─── 2. BATCH UNOS ────────────────────────────────────────────────────────

    @Transactional
    public AnalyticsStatsDTO.BatchResponse batchCreate(AnalyticsStatsDTO.BatchRequest req) {
        List<AnalyticsEventDTO.Response> results = req.getEvents().stream()
                .map(this::create)
                .collect(Collectors.toList());

        return AnalyticsStatsDTO.BatchResponse.builder()
                .created(results.size())
                .results(results)
                .build();
    }

    // ─── 3. CUSTOM UPITI ─────────────────────────────────────────────────────

    public AnalyticsStatsDTO.EventTypeCount getStats() {
        List<Object[]> rows = analyticsEventRepository.countByEventType();
        Map<String, Long> countMap = new HashMap<>();
        long total = 0;

        for (Object[] row : rows) {
            String type = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            countMap.put(type, count);
            total += count;
        }

        return AnalyticsStatsDTO.EventTypeCount.builder()
                .countByEventType(countMap)
                .totalEvents(total)
                .build();
    }

    public List<AnalyticsEventDTO.Response> getRecentEvents(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return analyticsEventRepository.findRecentEvents(since).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AnalyticsStatsDTO.ActiveUser> getMostActiveUsers(int topN) {
        Pageable pageable = PageRequest.of(0, topN);
        return analyticsEventRepository.findMostActiveUsers(pageable).stream()
                .map(row -> AnalyticsStatsDTO.ActiveUser.builder()
                        .userId(((Number) row[0]).longValue())
                        .eventCount(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────

    private AnalyticsEvent findOrThrow(Long id) {
        return analyticsEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AnalyticsEvent sa ID " + id + " nije pronađen"));
    }

    public AnalyticsEventDTO.Response toResponse(AnalyticsEvent e) {
        return AnalyticsEventDTO.Response.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .eventType(e.getEventType())
                .referenceId(e.getReferenceId())
                .referenceType(e.getReferenceType())
                .metadata(e.getMetadata())
                .createdAt(e.getCreatedAt())
                .build();
    }
}