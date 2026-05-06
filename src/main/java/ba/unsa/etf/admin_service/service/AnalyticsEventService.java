package ba.unsa.etf.admin_service.service;

import ba.unsa.etf.admin_service.dto.AnalyticsEventDTO;
import ba.unsa.etf.admin_service.exception.ResourceNotFoundException;
import ba.unsa.etf.admin_service.model.AnalyticsEvent;
import ba.unsa.etf.admin_service.model.EventType;
import ba.unsa.etf.admin_service.repository.AnalyticsEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsEventService {

    private final AnalyticsEventRepository analyticsEventRepository;

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

    private AnalyticsEvent findOrThrow(Long id) {
        return analyticsEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AnalyticsEvent sa ID " + id + " nije pronađen"));
    }

    private AnalyticsEventDTO.Response toResponse(AnalyticsEvent e) {
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