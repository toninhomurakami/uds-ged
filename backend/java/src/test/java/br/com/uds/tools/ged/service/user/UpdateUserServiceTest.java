package br.com.uds.tools.ged.service.user;

import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.dto.UserRequest;
import br.com.uds.tools.ged.dto.UserResponse;
import br.com.uds.tools.ged.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdateUserService updateUserService;

    @Test
    void update_whenValid_savesAndReturnsResponse() {
        User existing = new User(1L, "Old", "olduser", "enc", Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(existing)).thenReturn(existing);

        UserRequest request = new UserRequest();
        request.setName("New Name");
        request.setUsername("newuser");
        request.setPassword("newpass");
        request.setRole(Role.ADMIN);
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNew");

        UserResponse result = updateUserService.update(1L, request);

        assertThat(result).isNotNull();
        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getUsername()).isEqualTo("newuser");
        assertThat(existing.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).save(existing);
    }

    @Test
    void update_whenUserNotFound_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        UserRequest request = new UserRequest();
        request.setName("A");
        request.setUsername("a");
        request.setRole(Role.USER);

        assertThatThrownBy(() -> updateUserService.update(999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    void update_whenNewUsernameTakenByOther_throws() {
        User existing = new User(1L, "Old", "olduser", "enc", Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        UserRequest request = new UserRequest();
        request.setName("A");
        request.setUsername("taken");
        request.setRole(Role.USER);

        assertThatThrownBy(() -> updateUserService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome de usuário já existe");
    }

    @Test
    void update_whenPasswordNull_doesNotEncode() {
        User existing = new User(1L, "Old", "olduser", "enc", Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername("olduser")).thenReturn(false);
        when(userRepository.save(existing)).thenReturn(existing);

        UserRequest request = new UserRequest();
        request.setName("New");
        request.setUsername("olduser");
        request.setPassword(null);
        request.setRole(Role.USER);

        updateUserService.update(1L, request);

        assertThat(existing.getPassword()).isEqualTo("enc");
    }
}
