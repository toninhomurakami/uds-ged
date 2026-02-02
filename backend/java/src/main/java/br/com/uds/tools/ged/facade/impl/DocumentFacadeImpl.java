package br.com.uds.tools.ged.facade.impl;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.domain.DocumentVersion;
import br.com.uds.tools.ged.dto.DocumentRequest;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.dto.DocumentVersionResponse;
import br.com.uds.tools.ged.dto.PageResponse;
import br.com.uds.tools.ged.facade.DocumentFacade;
import br.com.uds.tools.ged.facade.FileStorageFacade;
import br.com.uds.tools.ged.service.FileDownload;
import br.com.uds.tools.ged.service.document.*;
import br.com.uds.tools.ged.service.storage.file.DeleteByFileKeyStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
@Transactional
@RequiredArgsConstructor
public class DocumentFacadeImpl implements DocumentFacade {

    private final FindAllDocumentService findAllDocumentService;
    private final FindByIdDocumentService findByIdDocumentService;
    private final CreateDocumentService createDocumentService;
    private final UpdateDocumentService updateDocumentService;
    private final PublishDocumentService publishDocumentService;
    private final ArchiveDocumentService archiveDocumentService;
    private final DeleteByIdDocumentService deleteByIdDocumentService;
    private final ListVersionsDocumentService listVersionsDocumentService;
    private final UploadVersionDocumentService uploadVersionDocumentService;
    private final DownloadCurrentDocumentService downloadCurrentDocumentService;
    private final DownloadVersionDocumentService downloadVersionDocumentService;
    private final DeleteByFileKeyStorageService deleteByFileKeyStorageService;
    private final FileStorageFacade fileStorageFacade;

    @Override
    public PageResponse<DocumentResponse> findAll(String title, DocumentStatus status, Pageable pageable) {
        return findAllDocumentService.findAll(title, status, pageable);
    }

    @Override
    public DocumentResponse findById(Long id) {
        return findByIdDocumentService.findById(id);
    }

    @Override
    public DocumentResponse create(DocumentRequest request) {
        return createDocumentService.create(request);
    }

    @Override
    public DocumentResponse update(Long id, DocumentRequest request) {
        return updateDocumentService.update(id, request);
    }

    @Override
    public DocumentResponse publish(Long id) {
        return publishDocumentService.publish(id);
    }

    @Override
    public DocumentResponse archive(Long id) {
        return archiveDocumentService.archive(id);
    }

    @Override
    public void deleteById(Long id) {
        Document document = findByIdDocumentService.findDocumentById(id);
        if (!deleteByIdDocumentService.isDeleteAllowed(id)) {
            return;
        }
        for (DocumentVersion version : document.getVersions()) {
            try {
                deleteByFileKeyStorageService.deleteByFileKey(version.getFileKey());
            } catch (IOException e) {
                System.err.println(String.format("Erro ao remover arquivo %s. A sequencia prosseguirá com os próximos, se houverem -> %s",
                        version.getFileKey(),
                        e.getMessage()));
            }
        }
        deleteByIdDocumentService.deleteById(id);
    }

    @Override
    public List<DocumentVersionResponse> listVersions(Long documentId) {
        return listVersionsDocumentService.listVersions(documentId);
    }

    @Override
    public DocumentVersionResponse uploadVersion(Long documentId, MultipartFile file) throws IOException {
        Document doc = findByIdDocumentService.findDocumentById(documentId);
        if (!uploadVersionDocumentService.isUploadAllowed(doc)) {
            throw new IllegalArgumentException("Acesso negado");
        }
        DocumentVersion version = uploadVersionDocumentService.uploadDocumentVersion(doc);

        String fileKey = fileStorageFacade.store(doc.getId(), version.getId(), file);
        version.setFileKey(fileKey);

        return uploadVersionDocumentService.getVersionResponse(version);
    }

    @Override
    public FileDownload downloadCurrent(Long documentId) throws IOException {
        Document doc = findByIdDocumentService.findDocumentById(documentId);
        if (!downloadCurrentDocumentService.isDownloadAllowed(doc)) {
            throw new IllegalArgumentException("Acesso negado");
        }
        if (doc.getVersions().isEmpty()) throw new IllegalArgumentException("Documento não possui arquivo anexado");
        DocumentVersion currentVersion = downloadCurrentDocumentService.getCurrentDocumentVersion(doc);

        byte[] content = getBytes(currentVersion.getFileKey());
        return downloadCurrentDocumentService.getCurrentFileDownload(doc, currentVersion, content);
    }

    @Override
    public FileDownload downloadVersion(Long documentId, Long versionId) throws IOException {
        Document doc = findByIdDocumentService.findDocumentById(documentId);
        if (!downloadVersionDocumentService.isDownloadAllowed(doc)) {
            throw new IllegalArgumentException("Acesso negado");
        }
        DocumentVersion documentVersion = downloadVersionDocumentService.getDocumentVersion(doc, versionId);

        byte[] content = getBytes(documentVersion.getFileKey());
        return downloadVersionDocumentService.getFileDownload(doc, documentVersion, content);
    }

    private byte[] getBytes(String fileKey) throws IOException {
        Path path = fileStorageFacade.resolve(fileKey);
        if (!Files.exists(path)) throw new IllegalArgumentException("Arquivo não encontrado no storage");
        return Files.readAllBytes(path);
    }
}
