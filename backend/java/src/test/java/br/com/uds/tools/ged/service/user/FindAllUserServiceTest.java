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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FindAllUserService findAllUserService;

    @Test
    void findAll_returnsMappedResponses() {
        User user1 = new User(1L, "User One", "user1", "enc", Role.USER);
        User user2 = new User(2L, "User Two", "user2", "enc", Role.ADMIN);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponse> result = findAllUserService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getUsername()).isEqualTo("user1");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).findAll();
    }
}
