package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.dto.DocumentResponse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FindByIdDocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FindByIdDocumentService findByIdDocumentService;

    private User owner;
    private Document document;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner", "enc", Role.USER);
        document = new Document();
        document.setId(1L);
        document.setTitle("Doc");
        document.setOwner(owner);
        document.setStatus(DocumentStatus.PUBLISHED);
        UserPrincipal currentUser = new UserPrincipal(1L, "owner", Role.USER);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void findById_whenDocumentExists_returnsResponse() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        DocumentResponse result = findByIdDocumentService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Doc");
        verify(documentRepository).findById(1L);
    }

    @Test
    void findById_whenDocumentNotFound_throws() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> findByIdDocumentService.findById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Documento não encontrado");
    }

    @Test
    void findById_whenDraftAndNotOwner_throws() {
        document.setStatus(DocumentStatus.DRAFT);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal(99L, "other", Role.USER));

        assertThatThrownBy(() -> findByIdDocumentService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Acesso negado");
    }
}
