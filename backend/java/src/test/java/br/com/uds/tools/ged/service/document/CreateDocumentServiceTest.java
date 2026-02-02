package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.dto.DocumentRequest;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.repository.DocumentRepository;
import br.com.uds.tools.ged.repository.UserRepository;
import br.com.uds.tools.ged.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CreateDocumentService createDocumentService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner", "enc", Role.USER);
        UserPrincipal currentUser = new UserPrincipal(1L, "owner", Role.USER);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void create_whenValid_savesAndReturnsResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Document saved = new Document();
        saved.setId(1L);
        saved.setTitle("Doc");
        saved.setOwner(owner);
        saved.setStatus(DocumentStatus.DRAFT);
        when(documentRepository.save(any(Document.class))).thenReturn(saved);

        DocumentRequest request = new DocumentRequest();
        request.setTitle("Doc");
        request.setDescription("Desc");
        request.setTags(List.of("tag1"));

        DocumentResponse result = createDocumentService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Doc");
        verify(documentRepository).save(any(Document.class));
    }
}
