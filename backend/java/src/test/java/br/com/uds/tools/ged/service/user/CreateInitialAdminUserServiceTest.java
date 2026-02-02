package br.com.uds.tools.ged.service.user;

import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
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
class CreateInitialAdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateInitialAdminUserService createInitialAdminUserService;

    @Test
    void createInitialAdmin_whenNoUsers_savesAdminAndReturnsResponse() {
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("encoded");
        User saved = new User(1L, "Admin", "admin", "encoded", Role.ADMIN);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse result = createInitialAdminUserService.createInitialAdmin("Admin", "admin", "pass1234");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void createInitialAdmin_whenUsersExist_throws() {
        when(userRepository.count()).thenReturn(1L);

        assertThatThrownBy(() -> createInitialAdminUserService.createInitialAdmin("Admin", "admin", "pass"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Setup inicial já foi realizado");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createInitialAdmin_whenUsernameExists_throws() {
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() -> createInitialAdminUserService.createInitialAdmin("Admin", "admin", "pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome de usuário já existe");
        verify(userRepository, never()).save(any(User.class));
    }
}
