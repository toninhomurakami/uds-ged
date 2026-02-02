package br.com.uds.tools.ged.facade;

import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.dto.DocumentRequest;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.dto.DocumentVersionResponse;
import br.com.uds.tools.ged.dto.PageResponse;
import br.com.uds.tools.ged.service.FileDownload;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentFacade {

    PageResponse<DocumentResponse> findAll(String title, DocumentStatus status, Pageable pageable);

    DocumentResponse findById(Long id);

    DocumentResponse create(DocumentRequest request);

    DocumentResponse update(Long id, DocumentRequest request);

    DocumentResponse publish(Long id);

    DocumentResponse archive(Long id);

    void deleteById(Long id);

    List<DocumentVersionResponse> listVersions(Long documentId);

    DocumentVersionResponse uploadVersion(Long documentId, MultipartFile file) throws IOException;

    FileDownload downloadCurrent(Long documentId) throws IOException;

    FileDownload downloadVersion(Long documentId, Long versionId) throws IOException;


}
