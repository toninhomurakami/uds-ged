package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.domain.DocumentVersion;
import br.com.uds.tools.ged.facade.FileStorageFacade;
import br.com.uds.tools.ged.security.UserPrincipal;
import br.com.uds.tools.ged.service.FileDownload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DownloadCurrentDocumentService extends AbstractDocumentService {

    private final FileStorageFacade fileStorageFacade;

    public DocumentVersion getCurrentDocumentVersion(Document document) {
        DocumentVersion current = document.getVersions().stream()
                .filter(v -> {
                    Path p = fileStorageFacade.resolve(v.getFileKey());
                    return Files.exists(p) && Files.isRegularFile(p);
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Arquivo não encontrado no storage"));
        return current;
    }

    public FileDownload getCurrentFileDownload(Document doc, DocumentVersion current, byte[] content) {
        String filename = buildDownloadFilename(current, doc.getId());
        return new FileDownload(content, filename);
    }

    public boolean isDownloadAllowed(Document document) {
        UserPrincipal principal = currentUser();
        return !DocumentStatus.DRAFT.equals(document.getStatus()) ||
                principal.getId().equals( document.getOwner().getId() );
    }
}
