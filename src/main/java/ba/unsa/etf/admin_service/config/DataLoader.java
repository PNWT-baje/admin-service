package ba.unsa.etf.admin_service.config;

import ba.unsa.etf.admin_service.model.AnalyticsEvent;
import ba.unsa.etf.admin_service.model.Report;
import ba.unsa.etf.admin_service.model.UserSuspension;
import ba.unsa.etf.admin_service.repository.AnalyticsEventRepository;
import ba.unsa.etf.admin_service.repository.ReportRepository;
import ba.unsa.etf.admin_service.repository.UserSuspensionRepository;
import ba.unsa.etf.admin_service.model.ReportReason;
import ba.unsa.etf.admin_service.model.ReportStatus;
import ba.unsa.etf.admin_service.model.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final ReportRepository reportRepository;
    private final UserSuspensionRepository userSuspensionRepository;
    private final AnalyticsEventRepository analyticsEventRepository;


    @Bean
    @Profile("!prod")
    public CommandLineRunner loadData() {
        return args -> {
            if (reportRepository.count() > 0) {
                log.info("Baza već sadrži podatke — DataLoader preskočen.");
                return;
            }

            log.info("Učitavanje testnih podataka za Admin Service...");



            Report report1 = reportRepository.save(Report.builder()
                    .reporterUserId(1L)
                    .reportedUserId(3L)
                    .reason(ReportReason.HARASSMENT)
                    .description("Korisnik šalje uvredljive poruke.")
                    .status(ReportStatus.PENDING)
                    .build());

            Report report2 = reportRepository.save(Report.builder()
                    .reporterUserId(2L)
                    .reason(ReportReason.SPAM)
                    .description("Objava sadrži spam linkove.")
                    .status(ReportStatus.REVIEWED)
                    .reviewedByUserId(99L) // admin user ID
                    .build());

            Report report3 = reportRepository.save(Report.builder()
                    .reporterUserId(4L)
                    .reportedUserId(5L)
                    .reason(ReportReason.HATE_SPEECH)
                    .description("Govor mržnje u komentarima.")
                    .status(ReportStatus.RESOLVED)
                    .reviewedByUserId(99L)
                    .resolvedAt(LocalDateTime.now().minusDays(1))
                    .build());

            Report report4 = reportRepository.save(Report.builder()
                    .reporterUserId(3L)
                    .reportedCommentId(55L)
                    .reason(ReportReason.VIOLENCE)
                    .description("Komentar prikazuje nasilni sadržaj.")
                    .status(ReportStatus.DISMISSED)
                    .reviewedByUserId(99L)
                    .resolvedAt(LocalDateTime.now().minusHours(5))
                    .build());

            Report report5 = reportRepository.save(Report.builder()
                    .reporterUserId(1L)
                    .reason(ReportReason.NUDITY)
                    .description("Objava sadrži neprimjeren sadržaj.")
                    .status(ReportStatus.PENDING)
                    .build());

            log.info("Kreirana {} izvještaja.", reportRepository.count());



            userSuspensionRepository.save(UserSuspension.builder()
                    .userId(3L)
                    .suspendedByUserId(99L)
                    .reason("Ponavljano uznemiravanje korisnika.")
                    .suspendedUntil(LocalDateTime.now().plusDays(7))
                    .build());

            userSuspensionRepository.save(UserSuspension.builder()
                    .userId(5L)
                    .suspendedByUserId(99L)
                    .reason("Objavljivanje spam sadržaja.")
                    .suspendedUntil(LocalDateTime.now().plusDays(3))
                    .build());

            userSuspensionRepository.save(UserSuspension.builder()
                    .userId(2L)
                    .suspendedByUserId(99L)
                    .reason("Kršenje pravila zajednice — govor mržnje.")
                    .suspendedUntil(LocalDateTime.now().plusDays(30))
                    .build());


            userSuspensionRepository.save(UserSuspension.builder()
                    .userId(6L)
                    .suspendedByUserId(99L)
                    .reason("Kreiranje lažnih računa i prevara.")
                    .suspendedUntil(null)
                    .build());

            log.info("Kreirane {} suspenzije.", userSuspensionRepository.count());


            analyticsEventRepository.save(AnalyticsEvent.builder()
                    .userId(1L)
                    .eventType(EventType.POST_VIEW)
                    .referenceId(10L)
                    .referenceType("POST")
                    .metadata("{\"duration_ms\": 3200, \"source\": \"feed\"}")
                    .build());

            analyticsEventRepository.save(AnalyticsEvent.builder()
                    .userId(2L)
                    .eventType(EventType.PROFILE_VIEW)
                    .referenceId(3L)
                    .referenceType("USER")
                    .metadata("{\"source\": \"search\"}")
                    .build());

            analyticsEventRepository.save(AnalyticsEvent.builder()
                    .userId(1L)
                    .eventType(EventType.SEARCH)
                    .referenceId(null)
                    .referenceType("HASHTAG")
                    .metadata("{\"query\": \"#programiranje\", \"results\": 42}")
                    .build());

            analyticsEventRepository.save(AnalyticsEvent.builder()
                    .userId(4L)
                    .eventType(EventType.ENGAGEMENT)
                    .referenceId(10L)
                    .referenceType("POST")
                    .metadata("{\"action\": \"like\", \"reaction_type\": \"HEART\"}")
                    .build());

            analyticsEventRepository.save(AnalyticsEvent.builder()
                    .userId(5L)
                    .eventType(EventType.POST_VIEW)
                    .referenceId(20L)
                    .referenceType("POST")
                    .metadata("{\"duration_ms\": 1500, \"source\": \"explore\"}")
                    .build());

            analyticsEventRepository.save(AnalyticsEvent.builder()
                    .userId(3L)
                    .eventType(EventType.ENGAGEMENT)
                    .referenceId(15L)
                    .referenceType("POST")
                    .metadata("{\"action\": \"share\"}")
                    .build());

            log.info("Kreirano {} analytics eventa.", analyticsEventRepository.count());
            log.info("DataLoader završen uspješno.");
        };
    }
}