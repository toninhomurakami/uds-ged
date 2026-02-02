package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.repository.DocumentVersionRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadVersionDocumentServiceTest {

    @Mock
    private DocumentVersionRepository documentVersionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UploadVersionDocumentService uploadVersionDocumentService;

    private Document document;
    private User owner;
    private User currentUserEntity;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner", "enc", Role.USER);
        currentUserEntity = new User(1L, "Owner", "owner", "enc", Role.USER);
        document = new Document();
        document.setId(1L);
        document.setOwner(owner);
        document.setStatus(DocumentStatus.PUBLISHED);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal(1L, "owner", Role.USER));
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void uploadDocumentVersion_createsVersionAndReturns() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUserEntity));
        br.com.uds.tools.ged.domain.DocumentVersion savedVersion = new br.com.uds.tools.ged.domain.DocumentVersion();
        savedVersion.setId(10L);
        savedVersion.setDocument(document);
        savedVersion.setFileKey("1/0/pending");
        savedVersion.setUploadedBy(currentUserEntity);
        when(documentVersionRepository.saveAndFlush(any())).thenReturn(savedVersion);

        br.com.uds.tools.ged.domain.DocumentVersion result = uploadVersionDocumentService.uploadDocumentVersion(document);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(documentVersionRepository).saveAndFlush(any());
    }

    @Test
    void isUploadAllowed_whenPublished_returnsTrue() {
        document.setStatus(DocumentStatus.PUBLISHED);
        boolean result = uploadVersionDocumentService.isUploadAllowed(document);
        assertThat(result).isTrue();
    }

    @Test
    void isUploadAllowed_whenDraftAndOwner_returnsTrue() {
        document.setStatus(DocumentStatus.DRAFT);
        boolean result = uploadVersionDocumentService.isUploadAllowed(document);
        assertThat(result).isTrue();
    }
}
