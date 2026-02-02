package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.dto.DocumentRequest;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateDocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UpdateDocumentService updateDocumentService;

    private Document document;
    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner", "enc", Role.USER);
        document = new Document();
        document.setId(1L);
        document.setTitle("Old");
        document.setOwner(owner);
        document.setStatus(DocumentStatus.DRAFT);
        UserPrincipal currentUser = new UserPrincipal(1L, "owner", Role.USER);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void update_whenValid_savesAndReturnsResponse() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        DocumentRequest request = new DocumentRequest();
        request.setTitle("New Title");
        request.setDescription("New Desc");
        request.setTags(List.of("t1"));
        request.setStatus(DocumentStatus.PUBLISHED);

        DocumentResponse result = updateDocumentService.update(1L, request);

        assertThat(result).isNotNull();
        assertThat(document.getTitle()).isEqualTo("New Title");
        verify(documentRepository).save(document);
    }

    @Test
    void update_whenDocumentNotFound_throws() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());
        DocumentRequest request = new DocumentRequest();
        request.setTitle("T");

        assertThatThrownBy(() -> updateDocumentService.update(999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Documento não encontrado");
    }
}
