package ba.unsa.etf.admin_service.dto;

import ba.unsa.etf.admin_service.model.EventType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class AnalyticsEventDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long userId;

        @NotNull(message = "eventType je obavezan")
        private EventType eventType;

        private Long referenceId;
        private String referenceType;
        private String metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long userId;
        private EventType eventType;
        private Long referenceId;
        private String referenceType;
        private String metadata;
        private LocalDateTime createdAt;
    }
}