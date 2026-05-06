package ba.unsa.etf.admin_service.dto;

import ba.unsa.etf.admin_service.model.Report;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class ReportStatsDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCount {
        private Map<String, Long> countByStatus;
        private long totalReports;
        private long pendingReports;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchRequest {
        @NotEmpty(message = "Lista prijava ne smije biti prazna")
        @Valid
        private List<ReportDTO.Request> reports;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchResponse {
        private int created;
        private int failed;
        private List<ReportDTO.Response> results;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResolveAndSuspendRequest {
        @NotNull(message = "reportId je obavezan")
        private Long reportId;

        @NotNull(message = "adminUserId je obavezan")
        private Long adminUserId;

        private String suspensionReason;
        private java.time.LocalDateTime suspendedUntil;
    }
}