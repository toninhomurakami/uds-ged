package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.DocumentVersion;
import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.facade.FileStorageFacade;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DownloadCurrentDocumentServiceTest {

    @Mock
    private FileStorageFacade fileStorageFacade;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DownloadCurrentDocumentService downloadCurrentDocumentService;

    @BeforeEach
    void setUp() {
        UserPrincipal principal = new UserPrincipal(1L, "user", Role.USER);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentFileDownload_returnsFileDownloadWithContentAndFilename() {
        byte[] content = new byte[]{1, 2, 3};
        br.com.uds.tools.ged.domain.Document doc = new br.com.uds.tools.ged.domain.Document();
        doc.setId(1L);
        var result = downloadCurrentDocumentService.getCurrentFileDownload(doc, new DocumentVersion(), content);
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.filename()).isNotBlank();
    }

    @Test
    void isDownloadAllowed_whenPublished_returnsTrue() {
        br.com.uds.tools.ged.domain.Document doc = new br.com.uds.tools.ged.domain.Document();
        doc.setStatus(br.com.uds.tools.ged.domain.DocumentStatus.PUBLISHED);
        doc.setOwner(new User(1L, "O", "o", "enc", Role.USER));
        assertThat(downloadCurrentDocumentService.isDownloadAllowed(doc)).isTrue();
    }
}
