package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.domain.DocumentVersion;
import br.com.uds.tools.ged.security.UserPrincipal;
import br.com.uds.tools.ged.service.FileDownload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DownloadVersionDocumentService extends AbstractDocumentService {

    public FileDownload getFileDownload(Document document, DocumentVersion version, byte[] content) {
        String filename = buildDownloadFilename(version, document.getId());
        return new FileDownload(content, filename);
    }

    public DocumentVersion getDocumentVersion(Document document, Long versionId) {
        DocumentVersion version = document.getVersions().stream()
                .filter(v -> v.getId().equals(versionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Versão não encontrada"));
        return version;
    }

    public boolean isDownloadAllowed(Document document) {
        UserPrincipal principal = currentUser();
        return !DocumentStatus.DRAFT.equals(document.getStatus()) ||
                principal.getId().equals( document.getOwner().getId() );
    }

}
