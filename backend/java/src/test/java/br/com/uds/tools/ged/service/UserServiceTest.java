package br.com.uds.tools.ged.service;

import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.repository.UserRepository;
import br.com.uds.tools.ged.dto.UserRequest;
import br.com.uds.tools.ged.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void create_whenUsernameExists_shouldThrow() {
        UserRequest request = new UserRequest();
        request.setName("Test User");
        request.setUsername("existing");
        request.setPassword("pass123");
        request.setRole(Role.USER);
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já existe");
    }

    @Test
    void create_shouldEncodePasswordAndSave() {
        UserRequest request = new UserRequest();
        request.setName("New User");
        request.setUsername("newuser");
        request.setPassword("pass123");
        request.setRole(Role.USER);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        User saved = new User(1L, "New User", "newuser", "encoded", Role.USER);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse result = userService.create(request);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getName()).isEqualTo("New User");
        assertThat(result.getRole()).isEqualTo(Role.USER);
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("pass123");
    }

    @Test
    void create_whenPasswordBlank_shouldThrow() {
        UserRequest request = new UserRequest();
        request.setName("User");
        request.setUsername("user");
        request.setPassword("");
        request.setRole(Role.USER);
        when(userRepository.existsByUsername("user")).thenReturn(false);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Senha");
    }
}
