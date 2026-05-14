package ba.unsa.etf.admin_service.controller;

import ba.unsa.etf.admin_service.dto.UserSuspensionDTO;
import ba.unsa.etf.admin_service.exception.ResourceNotFoundException;
import ba.unsa.etf.admin_service.service.UserSuspensionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserSuspensionController.class)
class UserSuspensionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserSuspensionService suspensionService;

    private UserSuspensionDTO.Response sampleResponse() {
        return UserSuspensionDTO.Response.builder()
                .id(1L)
                .userId(2L)
                .suspendedByUserId(99L)
                .reason("Kršenje pravila zajednice")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();
    }

    // ✅ POST /api/suspensions — kreiranje suspenzije
    @Test
    void create_success() throws Exception {
        when(suspensionService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/suspensions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 2,
                                    "suspendedByUserId": 99,
                                    "reason": "Kršenje pravila zajednice"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(2));
    }

    // ❌ POST — validation fail (bez userId)
    @Test
    void create_missingUserId_returns400() throws Exception {
        mockMvc.perform(post("/api/suspensions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "suspendedByUserId": 99,
                                    "reason": "Test razlog"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"));
    }

    // ❌ POST — validation fail (bez reason)
    @Test
    void create_missingReason_returns400() throws Exception {
        mockMvc.perform(post("/api/suspensions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 2,
                                    "suspendedByUserId": 99
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"));
    }

    // ✅ GET /api/suspensions — lista svih
    @Test
    void getAll_success() throws Exception {
        when(suspensionService.getAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/suspensions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ✅ GET /api/suspensions/{id} — po ID-u
    @Test
    void getById_success() throws Exception {
        when(suspensionService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/suspensions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ❌ GET /api/suspensions/{id} — nije pronađen
    @Test
    void getById_notFound() throws Exception {
        when(suspensionService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Suspenzija sa ID 99 nije pronađena"));

        mockMvc.perform(get("/api/suspensions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    // ✅ GET /api/suspensions/user/{userId} — suspenzije korisnika
    @Test
    void getByUserId_success() throws Exception {
        when(suspensionService.getByUserId(2L)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/suspensions/user/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ✅ GET /api/suspensions/user/{userId}/active — provjera aktivne suspenzije
    @Test
    void isActive_suspended_returnsTrue() throws Exception {
        when(suspensionService.isUserSuspended(2L)).thenReturn(true);

        mockMvc.perform(get("/api/suspensions/user/2/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(true));
    }

    // ✅ GET /api/suspensions/user/{userId}/active — korisnik nije suspendovan
    @Test
    void isActive_notSuspended_returnsFalse() throws Exception {
        when(suspensionService.isUserSuspended(1L)).thenReturn(false);

        mockMvc.perform(get("/api/suspensions/user/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(false));
    }

    // ✅ DELETE /api/suspensions/{id}
    @Test
    void delete_success() throws Exception {
        doNothing().when(suspensionService).delete(1L);

        mockMvc.perform(delete("/api/suspensions/1"))
                .andExpect(status().isNoContent());
    }

    // ❌ DELETE — nije pronađen
    @Test
    void delete_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Suspenzija sa ID 99 nije pronađena"))
                .when(suspensionService).delete(99L);

        mockMvc.perform(delete("/api/suspensions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }
}
