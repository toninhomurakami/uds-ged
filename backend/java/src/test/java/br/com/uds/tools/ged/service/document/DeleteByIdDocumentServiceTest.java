package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.repository.DocumentRepository;
import br.com.uds.tools.ged.security.UserPrincipal;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeleteByIdDocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DeleteByIdDocumentService deleteByIdDocumentService;

    private Document document;
    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner", "enc", Role.USER);
        document = new Document();
        document.setId(1L);
        document.setOwner(owner);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal(1L, "owner", Role.ADMIN));
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void deleteById_whenOwnerAndAdmin_deletes() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        deleteByIdDocumentService.deleteById(1L);

        verify(documentRepository).delete(document);
    }

    @Test
    void deleteById_whenDocumentNotFound_throws() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteByIdDocumentService.deleteById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Documento não encontrado");
        verify(documentRepository, never()).delete(any());
    }

    @Test
    void deleteById_whenNotOwnerNorAdmin_throws() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal(99L, "other", Role.USER));

        assertThatThrownBy(() -> deleteByIdDocumentService.deleteById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Acesso negado");
        verify(documentRepository, never()).delete(any());
    }

    @Test
    void isDeleteAllowed_whenOwnerAndAdmin_returnsTrue() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        boolean result = deleteByIdDocumentService.isDeleteAllowed(1L);

        assertThat(result).isTrue();
    }

    @Test
    void isDeleteAllowed_whenNotOwner_returnsFalse() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal(99L, "other", Role.ADMIN));

        boolean result = deleteByIdDocumentService.isDeleteAllowed(1L);

        assertThat(result).isFalse();
    }
}
