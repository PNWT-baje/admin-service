package ba.unsa.etf.admin_service.dto;

import ba.unsa.etf.admin_service.model.ReportReason;
import ba.unsa.etf.admin_service.model.ReportStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;



public class ReportDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotNull(message = "reporterUserId je obavezan")
        private Long reporterUserId;

        private Long reportedUserId;
        private Long reportedPostId;
        private Long reportedCommentId;

        @NotNull(message = "reason je obavezan")
        private ReportReason reason;

        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long reporterUserId;
        private Long reportedUserId;
        private Long reportedPostId;
        private Long reportedCommentId;
        private ReportReason reason;
        private String description;
        private ReportStatus status;
        private Long reviewedByUserId;
        private LocalDateTime createdAt;
        private LocalDateTime resolvedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdate {
        @NotNull(message = "status je obavezan")
        private ReportStatus status;

        private Long reviewedByUserId;
    }
}