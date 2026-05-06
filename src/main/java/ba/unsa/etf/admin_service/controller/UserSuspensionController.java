package ba.unsa.etf.admin_service.controller;

import ba.unsa.etf.admin_service.dto.UserSuspensionDTO;
import ba.unsa.etf.admin_service.service.UserSuspensionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suspensions")
@RequiredArgsConstructor
public class UserSuspensionController {

    private final UserSuspensionService suspensionService;

    // POST /api/suspensions
    @PostMapping
    public ResponseEntity<UserSuspensionDTO.Response> create(@Valid @RequestBody UserSuspensionDTO.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(suspensionService.create(req));
    }

    // GET /api/suspensions
    @GetMapping
    public ResponseEntity<List<UserSuspensionDTO.Response>> getAll() {
        return ResponseEntity.ok(suspensionService.getAll());
    }

    // GET /api/suspensions/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserSuspensionDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(suspensionService.getById(id));
    }

    // GET /api/suspensions/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserSuspensionDTO.Response>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(suspensionService.getByUserId(userId));
    }

    // GET /api/suspensions/user/{userId}/active
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<Map<String, Boolean>> isActive(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("suspended", suspensionService.isUserSuspended(userId)));
    }

    // DELETE /api/suspensions/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        suspensionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}