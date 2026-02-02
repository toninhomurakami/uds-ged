package br.com.uds.tools.ged.service.user;

import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetByUsernameUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetByUsernameUserService getByUsernameUserService;

    @Test
    void getByUsername_whenUserExists_returnsUser() {
        User user = new User(1L, "Admin", "admin", "enc", Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        User result = getByUsernameUserService.getByUsername("admin");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("admin");
        verify(userRepository).findByUsername("admin");
    }

    @Test
    void getByUsername_whenUserNotFound_throws() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getByUsernameUserService.getByUsername("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciais inválidas");
        verify(userRepository).findByUsername("unknown");
    }
}
