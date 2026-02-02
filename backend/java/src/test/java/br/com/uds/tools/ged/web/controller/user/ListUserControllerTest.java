package br.com.uds.tools.ged.web.controller.user;

import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.dto.UserResponse;
import br.com.uds.tools.ged.facade.UserFacade;
import br.com.uds.tools.ged.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ListUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class ListUserControllerTest {

    @MockBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserFacade userFacade;

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_returns200AndUserList() throws Exception {
        List<UserResponse> users = List.of(new UserResponse(1L, "Admin", "admin", Role.ADMIN));
        when(userFacade.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("admin"));

        verify(userFacade).findAll();
    }
}
