package br.com.uds.tools.ged.service.user;

import br.com.uds.tools.ged.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CountUserService countUserService;

    @Test
    void count_returnsRepositoryCount() {
        when(userRepository.count()).thenReturn(5L);

        long result = countUserService.count();

        assertThat(result).isEqualTo(5L);
        verify(userRepository).count();
    }
}
