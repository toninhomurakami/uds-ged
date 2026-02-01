package br.com.uds.tools.ged.security;

import br.com.uds.tools.ged.config.JwtProperties;
import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("ged-secret-key-change-in-production-min-256-bits");
        properties.setExpirationMs(3600000L);
        jwtService = new JwtService(properties);
    }

    @Test
    void generateToken_shouldProduceValidToken() {
        User user = new User(1L, "Admin", "admin", "encoded", Role.ADMIN);
        String token = jwtService.generateToken(user);
        assertThat(token).isNotBlank();
    }

    @Test
    void getUsername_shouldReturnSubjectFromToken() {
        User user = new User(2L, "User", "johndoe", "enc", Role.USER);
        String token = jwtService.generateToken(user);
        assertThat(jwtService.getUsername(token)).isEqualTo("johndoe");
    }

    @Test
    void getUserId_shouldReturnUserIdClaim() {
        User user = new User(42L, "Name", "user", "enc", Role.ADMIN);
        String token = jwtService.generateToken(user);
        assertThat(jwtService.getUserId(token)).isEqualTo(42L);
    }

    @Test
    void getRole_shouldReturnRoleClaim() {
        User user = new User(1L, "Admin", "admin", "enc", Role.ADMIN);
        String token = jwtService.generateToken(user);
        assertThat(jwtService.getRole(token)).isEqualTo(Role.ADMIN);
    }

    @Test
    void getUsername_whenTokenInvalid_shouldThrow() {
        assertThatThrownBy(() -> jwtService.getUsername("invalid-token"))
                .hasMessageContaining("JWT");
    }

    @Test
    void getUserId_whenTokenInvalid_shouldThrow() {
        assertThatThrownBy(() -> jwtService.getUserId("invalid-token"))
                .hasMessageContaining("JWT");
    }

    @Test
    void getRole_whenTokenInvalid_shouldThrow() {
        assertThatThrownBy(() -> jwtService.getRole("invalid-token"))
                .hasMessageContaining("JWT");
    }
}
