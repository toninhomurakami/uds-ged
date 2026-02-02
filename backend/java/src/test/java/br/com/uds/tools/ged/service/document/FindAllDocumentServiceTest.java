package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.dto.PageResponse;
import br.com.uds.tools.ged.repository.DocumentRepository;
import br.com.uds.tools.ged.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllDocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FindAllDocumentService findAllDocumentService;

    @BeforeEach
    void setUp() {
        UserPrincipal currentUser = new UserPrincipal(1L, "user", Role.USER);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void findAll_returnsPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Document> page = new PageImpl<>(List.of(), pageable, 0);
        when(documentRepository.findAllFiltered(eq(null), eq(null), eq(1L), eq(pageable))).thenReturn(page);

        PageResponse<?> result = findAllDocumentService.findAll(null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(documentRepository).findAllFiltered(any(), any(), eq(1L), eq(pageable));
    }
}
