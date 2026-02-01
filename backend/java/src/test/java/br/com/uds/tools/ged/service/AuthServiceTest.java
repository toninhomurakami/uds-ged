package br.com.uds.tools.ged.service;

import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.security.JwtService;
import br.com.uds.tools.ged.dto.LoginRequest;
import br.com.uds.tools.ged.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_whenPasswordInvalid_shouldThrow() {
        User user = new User(1L, "User", "user", "encoded", Role.USER);
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("wrong");
        when(userService.getByUsername("user")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciais inválidas");
    }

    @Test
    void login_whenValid_shouldReturnTokenAndRole() {
        User user = new User(1L, "Admin", "admin", "encoded", Role.ADMIN);
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("pass");
        when(userService.getByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("pass", "encoded")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
    }
}
