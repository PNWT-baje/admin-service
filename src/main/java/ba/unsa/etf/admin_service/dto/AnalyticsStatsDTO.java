package ba.unsa.etf.admin_service.dto;

import ba.unsa.etf.admin_service.model.AnalyticsEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class AnalyticsStatsDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventTypeCount {
        private Map<String, Long> countByEventType;
        private long totalEvents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveUser {
        private Long userId;
        private Long eventCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchRequest {
        @NotEmpty(message = "Lista eventa ne smije biti prazna")
        @Valid
        private List<AnalyticsEventDTO.Request> events;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchResponse {
        private int created;
        private List<AnalyticsEventDTO.Response> results;
    }
}