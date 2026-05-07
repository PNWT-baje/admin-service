package ba.unsa.etf.admin_service.service;

import ba.unsa.etf.admin_service.client.RemoteUserClient;
import ba.unsa.etf.admin_service.dto.UserSuspensionDTO;
import ba.unsa.etf.admin_service.exception.ResourceNotFoundException;
import ba.unsa.etf.admin_service.model.UserSuspension;
import ba.unsa.etf.admin_service.repository.UserSuspensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSuspensionService {

    private final UserSuspensionRepository suspensionRepository;
    private final RemoteUserClient remoteUserClient;

    public UserSuspensionDTO.Response create(UserSuspensionDTO.Request req) {
        // Task 5: fail-open verifikacija — ako user-service nije dostupan, nastavljamo.
        // Ako user-service eksplicitno kaže da korisnik ne postoji, vraćamo 400.
        if (!remoteUserClient.userExists(req.getUserId())) {
            throw new ResourceNotFoundException("Korisnik sa ID " + req.getUserId() + " ne postoji u sistemu");
        }

        UserSuspension suspension = UserSuspension.builder()
                .userId(req.getUserId())
                .suspendedByUserId(req.getSuspendedByUserId())
                .reason(req.getReason())
                .suspendedUntil(req.getSuspendedUntil())
                .build();
        return toResponse(suspensionRepository.save(suspension));
    }

    public List<UserSuspensionDTO.Response> getAll() {
        return suspensionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserSuspensionDTO.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<UserSuspensionDTO.Response> getByUserId(Long userId) {
        return suspensionRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public boolean isUserSuspended(Long userId) {
        return !suspensionRepository.findActiveByUserId(userId, LocalDateTime.now()).isEmpty();
    }

    public void delete(Long id) {
        findOrThrow(id);
        suspensionRepository.deleteById(id);
    }

    private UserSuspension findOrThrow(Long id) {
        return suspensionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserSuspension sa ID " + id + " nije pronađena"));
    }

    private UserSuspensionDTO.Response toResponse(UserSuspension s) {
        boolean active = s.getSuspendedUntil() == null ||
                s.getSuspendedUntil().isAfter(LocalDateTime.now());
        return UserSuspensionDTO.Response.builder()
                .id(s.getId())
                .userId(s.getUserId())
                .suspendedByUserId(s.getSuspendedByUserId())
                .reason(s.getReason())
                .suspendedUntil(s.getSuspendedUntil())
                .createdAt(s.getCreatedAt())
                .active(active)
                .build();
    }
}