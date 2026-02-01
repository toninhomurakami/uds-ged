package br.com.uds.tools.ged.web.controller;

import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.dto.UserResponse;
import br.com.uds.tools.ged.security.JwtService;
import br.com.uds.tools.ged.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SetupController.class)
@AutoConfigureMockMvc(addFilters = false)
class SetupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private UserService userService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private JwtService jwtService;

    @Test
    void status_whenNoUsers_returnsNeedsSetupTrue() throws Exception {
        when(userService.count()).thenReturn(0L);

        mockMvc.perform(get("/api/setup/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needsSetup").value(true));

        verify(userService).count();
    }

    @Test
    void status_whenUsersExist_returnsNeedsSetupFalse() throws Exception {
        when(userService.count()).thenReturn(1L);

        mockMvc.perform(get("/api/setup/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needsSetup").value(false));

        verify(userService).count();
    }

    @Test
    void createInitialAdmin_whenValid_returns201AndUser() throws Exception {
        Map<String, String> body = Map.of(
                "name", "Admin",
                "username", "admin",
                "password", "pass1234"
        );
        UserResponse created = new UserResponse(1L, "Admin", "admin", Role.ADMIN);
        when(userService.createInitialAdmin(eq("Admin"), eq("admin"), eq("pass1234"))).thenReturn(created);

        mockMvc.perform(post("/api/setup/initial-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.name").value("Admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService).createInitialAdmin("Admin", "admin", "pass1234");
    }

    @Test
    void createInitialAdmin_whenSetupAlreadyDone_returns400() throws Exception {
        Map<String, String> body = Map.of(
                "name", "Admin",
                "username", "admin",
                "password", "pass1234"
        );
        when(userService.createInitialAdmin(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalStateException("Setup inicial já foi realizado."));

        mockMvc.perform(post("/api/setup/initial-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Setup inicial já foi realizado."));
    }

    @Test
    void createInitialAdmin_whenUsernameExists_returns400() throws Exception {
        Map<String, String> body = Map.of(
                "name", "Admin",
                "username", "admin",
                "password", "pass1234"
        );
        when(userService.createInitialAdmin(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Nome de usuário já existe"));

        mockMvc.perform(post("/api/setup/initial-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nome de usuário já existe"));
    }
}
