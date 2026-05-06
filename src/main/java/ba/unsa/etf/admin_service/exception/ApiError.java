package ba.unsa.etf.admin_service.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ApiError {
    private String error;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private List<String> details; // za validacijske greške
}