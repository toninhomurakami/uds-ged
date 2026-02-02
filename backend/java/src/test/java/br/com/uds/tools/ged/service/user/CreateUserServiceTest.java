package br.com.uds.tools.ged.service.user;

import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.dto.UserRequest;
import br.com.uds.tools.ged.dto.UserResponse;
import br.com.uds.tools.ged.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateUserService createUserService;

    @Test
    void create_whenValid_savesAndReturnsResponse() {
        UserRequest request = new UserRequest();
        request.setName("John");
        request.setUsername("john");
        request.setPassword("pass123");
        request.setRole(Role.USER);
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        User saved = new User(1L, "John", "john", "encoded", Role.USER);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse result = createUserService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("john");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("John");
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
    }

    @Test
    void create_whenUsernameExists_throws() {
        UserRequest request = new UserRequest();
        request.setName("John");
        request.setUsername("john");
        request.setPassword("pass123");
        request.setRole(Role.USER);
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> createUserService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome de usuário já existe");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_whenPasswordBlank_throws() {
        UserRequest request = new UserRequest();
        request.setName("John");
        request.setUsername("john");
        request.setPassword("");
        request.setRole(Role.USER);
        when(userRepository.existsByUsername("john")).thenReturn(false);

        assertThatThrownBy(() -> createUserService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Senha é obrigatória");
        verify(userRepository, never()).save(any(User.class));
    }
}
