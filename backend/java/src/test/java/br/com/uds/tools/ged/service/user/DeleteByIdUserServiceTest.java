package br.com.uds.tools.ged.service.user;

import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.facade.FileStorageFacade;
import br.com.uds.tools.ged.repository.DocumentRepository;
import br.com.uds.tools.ged.repository.DocumentVersionRepository;
import br.com.uds.tools.ged.repository.UserRepository;
import br.com.uds.tools.ged.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeleteByIdUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentVersionRepository documentVersionRepository;

    @Mock
    private FileStorageFacade fileStorageFacade;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DeleteByIdUserService deleteByIdUserService;

    private UserPrincipal currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new UserPrincipal(2L, "other", Role.ADMIN);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deleteById_whenUserExistsAndNotSelf_deletesUser() {
        User toDelete = new User(1L, "User", "user1", "enc", Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(toDelete));
        when(documentRepository.findByOwnerId(1L)).thenReturn(List.of());
        when(documentVersionRepository.findByUploadedById(1L)).thenReturn(List.of());

        deleteByIdUserService.deleteById(1L);

        verify(userRepository).delete(toDelete);
    }

    @Test
    void deleteById_whenUserNotFound_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteByIdUserService.deleteById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuário não encontrado");
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteById_whenDeletingSelf_throws() {
        User self = new User(2L, "Me", "me", "enc", Role.ADMIN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> deleteByIdUserService.deleteById(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Não é possível excluir o próprio usuário");
        verify(userRepository, never()).delete(any());
    }
}
