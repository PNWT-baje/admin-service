package ba.unsa.etf.admin_service.repository;

import ba.unsa.etf.admin_service.model.UserSuspension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDateTime;
import java.util.List;

public interface UserSuspensionRepository extends JpaRepository<UserSuspension, Long>
{

    List<UserSuspension> findByUserId(Long userId);

    List<UserSuspension> findBySuspendedByUserId(Long adminUserId);

    // Pronađi sve aktivne suspenzije (još nisu istekle)
    @Query("SELECT s FROM UserSuspension s WHERE s.userId = :userId AND (s.suspendedUntil IS NULL OR s.suspendedUntil > :now)")
    List<UserSuspension> findActiveByUserId(Long userId, LocalDateTime now);
}
