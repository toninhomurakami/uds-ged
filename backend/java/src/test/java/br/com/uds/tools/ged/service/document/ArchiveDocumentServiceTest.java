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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ArchiveDocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ArchiveDocumentService archiveDocumentService;

    private Document document;
    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner", "enc", Role.USER);
        document = new Document();
        document.setId(1L);
        document.setOwner(owner);
        document.setStatus(DocumentStatus.PUBLISHED);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal(1L, "owner", Role.USER));
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void archive_whenOwner_archivesAndReturnsResponse() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        DocumentResponse result = archiveDocumentService.archive(1L);

        assertThat(result).isNotNull();
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.ARCHIVED);
        verify(documentRepository).save(document);
    }

    @Test
    void archive_whenDocumentNotFound_throws() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> archiveDocumentService.archive(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Documento não encontrado");
        verify(documentRepository, never()).save(any());
    }

    @Test
    void archive_whenNotOwnerNorAdmin_throws() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal(99L, "other", Role.USER));

        assertThatThrownBy(() -> archiveDocumentService.archive(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Acesso negado");
        verify(documentRepository, never()).save(any());
    }
}
