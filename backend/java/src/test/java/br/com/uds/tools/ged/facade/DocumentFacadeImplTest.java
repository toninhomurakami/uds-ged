package br.com.uds.tools.ged.facade;

import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.dto.DocumentRequest;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.dto.DocumentVersionResponse;
import br.com.uds.tools.ged.dto.PageResponse;
import br.com.uds.tools.ged.facade.impl.DocumentFacadeImpl;
import br.com.uds.tools.ged.service.document.*;
import br.com.uds.tools.ged.service.storage.file.DeleteByFileKeyStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentFacadeImplTest {

    @Mock
    private FindAllDocumentService findAllDocumentService;

    @Mock
    private FindByIdDocumentService findByIdDocumentService;

    @Mock
    private CreateDocumentService createDocumentService;

    @Mock
    private UpdateDocumentService updateDocumentService;

    @Mock
    private PublishDocumentService publishDocumentService;

    @Mock
    private ArchiveDocumentService archiveDocumentService;

    @Mock
    private DeleteByIdDocumentService deleteByIdDocumentService;

    @Mock
    private ListVersionsDocumentService listVersionsDocumentService;

    @Mock
    private UploadVersionDocumentService uploadVersionDocumentService;

    @Mock
    private DownloadCurrentDocumentService downloadCurrentDocumentService;

    @Mock
    private DownloadVersionDocumentService downloadVersionDocumentService;

    @Mock
    private DeleteByFileKeyStorageService deleteByFileKeyStorageService;

    @Mock
    private FileStorageFacade fileStorageFacade;

    @InjectMocks
    private DocumentFacadeImpl documentFacade;

    @Test
    void findAll_delegatesToService() {
        Pageable pageable = PageRequest.of(0, 10);
        PageResponse<DocumentResponse> page = new PageResponse<>(List.of(), 0, 10, 0, 0, true, true);
        when(findAllDocumentService.findAll(any(), any(), eq(pageable))).thenReturn(page);

        PageResponse<DocumentResponse> result = documentFacade.findAll(null, null, pageable);

        assertThat(result).isNotNull();
        verify(findAllDocumentService).findAll(null, null, pageable);
    }

    @Test
    void findById_delegatesToService() {
        DocumentResponse doc = new DocumentResponse(1L, "Doc", "Desc", List.of(), 1L, "Owner", DocumentStatus.DRAFT, null, null, false);
        when(findByIdDocumentService.findById(1L)).thenReturn(doc);

        assertThat(documentFacade.findById(1L)).isEqualTo(doc);
        verify(findByIdDocumentService).findById(1L);
    }

    @Test
    void create_delegatesToService() {
        DocumentRequest request = new DocumentRequest();
        request.setTitle("Doc");
        DocumentResponse created = new DocumentResponse(1L, "Doc", "Desc", List.of(), 1L, "Owner", DocumentStatus.DRAFT, null, null, false);
        when(createDocumentService.create(any(DocumentRequest.class))).thenReturn(created);

        assertThat(documentFacade.create(request)).isEqualTo(created);
        verify(createDocumentService).create(request);
    }

    @Test
    void update_delegatesToService() {
        DocumentRequest request = new DocumentRequest();
        request.setTitle("New");
        DocumentResponse updated = new DocumentResponse(1L, "New", "Desc", List.of(), 1L, "Owner", DocumentStatus.DRAFT, null, null, false);
        when(updateDocumentService.update(eq(1L), any(DocumentRequest.class))).thenReturn(updated);

        assertThat(documentFacade.update(1L, request)).isEqualTo(updated);
        verify(updateDocumentService).update(1L, request);
    }

    @Test
    void publish_delegatesToService() {
        DocumentResponse published = new DocumentResponse(1L, "Doc", "Desc", List.of(), 1L, "Owner", DocumentStatus.PUBLISHED, null, null, true);
        when(publishDocumentService.publish(1L)).thenReturn(published);

        assertThat(documentFacade.publish(1L)).isEqualTo(published);
        verify(publishDocumentService).publish(1L);
    }

    @Test
    void archive_delegatesToService() {
        DocumentResponse archived = new DocumentResponse(1L, "Doc", "Desc", List.of(), 1L, "Owner", DocumentStatus.ARCHIVED, null, null, true);
        when(archiveDocumentService.archive(1L)).thenReturn(archived);

        assertThat(documentFacade.archive(1L)).isEqualTo(archived);
        verify(archiveDocumentService).archive(1L);
    }

    @Test
    void listVersions_delegatesToService() {
        List<DocumentVersionResponse> versions = List.of();
        when(listVersionsDocumentService.listVersions(1L)).thenReturn(versions);

        assertThat(documentFacade.listVersions(1L)).isEqualTo(versions);
        verify(listVersionsDocumentService).listVersions(1L);
    }
}
