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
class PublishDocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PublishDocumentService publishDocumentService;

    private Document document;
    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner", "enc", Role.USER);
        document = new Document();
        document.setId(1L);
        document.setOwner(owner);
        document.setStatus(DocumentStatus.DRAFT);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal(1L, "owner", Role.USER));
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void publish_whenOwner_publishesAndReturnsResponse() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        DocumentResponse result = publishDocumentService.publish(1L);

        assertThat(result).isNotNull();
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.PUBLISHED);
        verify(documentRepository).save(document);
    }

    @Test
    void publish_whenDocumentNotFound_throws() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publishDocumentService.publish(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Documento não encontrado");
        verify(documentRepository, never()).save(any());
    }

    @Test
    void publish_whenNotOwnerNorAdmin_throws() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal(99L, "other", Role.USER));

        assertThatThrownBy(() -> publishDocumentService.publish(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Acesso negado");
        verify(documentRepository, never()).save(any());
    }
}
