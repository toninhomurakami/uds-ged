package br.com.uds.tools.ged.service;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.repository.DocumentRepository;
import br.com.uds.tools.ged.repository.UserRepository;
import br.com.uds.tools.ged.security.UserPrincipal;
import br.com.uds.tools.ged.storage.FileStorageService;
import br.com.uds.tools.ged.dto.DocumentRequest;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private DocumentService documentService;

    private User owner;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner", null, Role.USER);
        principal = new UserPrincipal(1L, "owner", Role.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    void findAll_shouldReturnPageFilteredByCurrentUser() {
        Document doc = new Document();
        doc.setId(1L);
        doc.setTitle("Doc 1");
        doc.setDescription("Desc");
        doc.setOwner(owner);
        doc.setStatus(DocumentStatus.PUBLISHED);
        doc.setCreatedAt(Instant.now());
        doc.setUpdatedAt(Instant.now());
        Page<Document> page = new PageImpl<>(List.of(doc));
        when(documentRepository.findAllFiltered(eq(null), eq(null), eq(1L), any(Pageable.class))).thenReturn(page);

        PageResponse<DocumentResponse> result = documentService.findAll(null, null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Doc 1");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findById_whenDraftAndNotOwner_shouldThrow() {
        User other = new User(2L, "Other", "other", null, Role.USER);
        Document doc = new Document();
        doc.setId(1L);
        doc.setTitle("Draft");
        doc.setOwner(other);
        doc.setStatus(DocumentStatus.DRAFT);
        doc.setCreatedAt(Instant.now());
        doc.setUpdatedAt(Instant.now());
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> documentService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rascunho");
    }

    @Test
    void create_shouldSetOwnerAndPersist() {
        DocumentRequest request = new DocumentRequest();
        request.setTitle("Novo Doc");
        request.setDescription("Desc");
        request.setStatus(DocumentStatus.DRAFT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Document saved = new Document();
        saved.setId(1L);
        saved.setTitle("Novo Doc");
        saved.setOwner(owner);
        saved.setStatus(DocumentStatus.DRAFT);
        saved.setCreatedAt(Instant.now());
        saved.setUpdatedAt(Instant.now());
        when(documentRepository.save(any(Document.class))).thenReturn(saved);

        DocumentResponse result = documentService.create(request);

        assertThat(result.getTitle()).isEqualTo("Novo Doc");
        assertThat(result.getOwnerId()).isEqualTo(1L);
        verify(documentRepository).save(any(Document.class));
    }
}
