package ba.unsa.etf.admin_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserSuspensionDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotNull(message = "userId je obavezan")
        private Long userId;

        @NotNull(message = "suspendedByUserId je obavezan")
        private Long suspendedByUserId;

        @NotBlank(message = "reason je obavezan")
        private String reason;

        private LocalDateTime suspendedUntil; // null = permanentna suspenzija
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long userId;
        private Long suspendedByUserId;
        private String reason;
        private LocalDateTime suspendedUntil;
        private LocalDateTime createdAt;
        private boolean active;
    }
}