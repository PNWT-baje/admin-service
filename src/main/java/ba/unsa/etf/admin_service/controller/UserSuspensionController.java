package ba.unsa.etf.admin_service.controller;

import ba.unsa.etf.admin_service.dto.UserSuspensionDTO;
import ba.unsa.etf.admin_service.service.UserSuspensionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suspensions")
@RequiredArgsConstructor
public class UserSuspensionController {

    private final UserSuspensionService suspensionService;

    /** Suspend a user — moderators and above. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<UserSuspensionDTO.Response> create(@Valid @RequestBody UserSuspensionDTO.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(suspensionService.create(req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<UserSuspensionDTO.Response>> getAll() {
        return ResponseEntity.ok(suspensionService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<UserSuspensionDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(suspensionService.getById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<UserSuspensionDTO.Response>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(suspensionService.getByUserId(userId));
    }

    /** Check if a user is currently suspended — accessible to all roles (needed by other services). */
    @GetMapping("/user/{userId}/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> isActive(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("suspended", suspensionService.isUserSuspended(userId)));
    }

    /** Lift a suspension — moderators and above. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        suspensionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
