package br.com.uds.tools.ged.service.user;

import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.dto.UserResponse;
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
class FindByIdUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FindByIdUserService findByIdUserService;

    @Test
    void findById_whenUserExists_returnsResponse() {
        User user = new User(1L, "Admin", "admin", "enc", Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = findByIdUserService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_whenUserNotFound_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> findByIdUserService.findById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuário não encontrado");
        verify(userRepository).findById(999L);
    }
}
