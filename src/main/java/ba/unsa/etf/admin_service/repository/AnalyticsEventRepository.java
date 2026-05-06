package ba.unsa.etf.admin_service.repository;

import ba.unsa.etf.admin_service.model.AnalyticsEvent;
import ba.unsa.etf.admin_service.model.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    List<AnalyticsEvent> findByUserId(Long userId);

    List<AnalyticsEvent> findByEventType(EventType eventType);

    // Paginacija
    Page<AnalyticsEvent> findAll(Pageable pageable);

    Page<AnalyticsEvent> findByEventType(EventType eventType, Pageable pageable);

    // Custom: broj eventa po tipu
    @Query("SELECT e.eventType, COUNT(e) FROM AnalyticsEvent e GROUP BY e.eventType")
    List<Object[]> countByEventType();

    // Custom: eventi u zadnjih N sati
    @Query("SELECT e FROM AnalyticsEvent e WHERE e.createdAt >= :since ORDER BY e.createdAt DESC")
    List<AnalyticsEvent> findRecentEvents(@Param("since") LocalDateTime since);

    // Custom: top N korisnika po broju eventa
    @Query("SELECT e.userId, COUNT(e) as cnt FROM AnalyticsEvent e GROUP BY e.userId ORDER BY cnt DESC")
    List<Object[]> findMostActiveUsers(Pageable pageable);

    List<AnalyticsEvent> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}