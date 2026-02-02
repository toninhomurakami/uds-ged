package br.com.uds.tools.ged.web.controller.user;

import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.dto.UserRequest;
import br.com.uds.tools.ged.dto.UserResponse;
import br.com.uds.tools.ged.facade.UserFacade;
import br.com.uds.tools.ged.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UpdateUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UpdateUserControllerTest {

    @MockBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserFacade userFacade;

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_whenValid_returns200() throws Exception {
        UserResponse updated = new UserResponse(1L, "New Name", "user1", Role.USER);
        when(userFacade.update(eq(1L), any(UserRequest.class))).thenReturn(updated);

        UserRequest request = new UserRequest();
        request.setName("New Name");
        request.setUsername("user1");
        request.setRole(Role.USER);

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));

        verify(userFacade).update(eq(1L), any(UserRequest.class));
    }
}
