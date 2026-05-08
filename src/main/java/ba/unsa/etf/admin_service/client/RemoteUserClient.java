package ba.unsa.etf.admin_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Component
public class RemoteUserClient {

    private static final Logger log = LoggerFactory.getLogger(RemoteUserClient.class);
    private final RestClient restClient;

    public RemoteUserClient(@Qualifier("lbRestClientBuilder") RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://user-service").build();
    }

    // Fail-open za admin operacije: admin-service ne smije biti blokiran nedostupnošću
    // user-service. Ako user-service ne odgovori, nastavljamo sa suspenzijom.
    // Vraća false samo ako user-service eksplicitno kaže da user ne postoji (404).
    public boolean userExists(Long userId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Boolean> body = restClient.get()
                    .uri("/api/users/{id}/exists", userId)
                    .retrieve()
                    .body(Map.class);
            return body != null && Boolean.TRUE.equals(body.get("exists"));
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            log.warn("user-service nedostupan za userId={}: {} — admin operacija nastavlja (fail-open)",
                    userId, e.getMessage());
            return true;
        }
    }
}
