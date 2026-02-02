package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.domain.DocumentVersion;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.dto.DocumentVersionResponse;
import br.com.uds.tools.ged.repository.DocumentVersionRepository;
import br.com.uds.tools.ged.repository.UserRepository;
import br.com.uds.tools.ged.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UploadVersionDocumentService extends AbstractDocumentService {

    private final DocumentVersionRepository documentVersionRepository;
    private final UserRepository userRepository;

    public DocumentVersion uploadDocumentVersion(Document doc) {
        UserPrincipal principal = currentUser();
        User uploadedBy = userRepository.findById(principal.getId()).orElseThrow();
        // Persiste a versão primeiro para obter o ID; depois grava o arquivo e atualiza o fileKey
        DocumentVersion version = new DocumentVersion();
        version.setDocument(doc);
        version.setFileKey(doc.getId() + "/0/pending");
        version.setUploadedAt(java.time.Instant.now());
        version.setUploadedBy(uploadedBy);
        version = documentVersionRepository.saveAndFlush(version);

        return version;
    }

    public DocumentVersionResponse getVersionResponse(DocumentVersion version) {
        documentVersionRepository.saveAndFlush(version);
        return toVersionResponse(version);
    }

    public boolean isUploadAllowed(Document document) {
        UserPrincipal principal = currentUser();
        return !DocumentStatus.DRAFT.equals(document.getStatus()) ||
                principal.getId().equals(document.getOwner().getId());
    }
}
