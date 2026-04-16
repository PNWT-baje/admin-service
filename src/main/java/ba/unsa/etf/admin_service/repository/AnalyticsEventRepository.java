package ba.unsa.etf.admin_service.repository;

import ba.unsa.etf.admin_service.model.AnalyticsEvent;
import ba.unsa.etf.admin_service.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    List<AnalyticsEvent> findByUserId(Long userId);

    List<AnalyticsEvent> findByEventType(EventType eventType);

    List<AnalyticsEvent> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}