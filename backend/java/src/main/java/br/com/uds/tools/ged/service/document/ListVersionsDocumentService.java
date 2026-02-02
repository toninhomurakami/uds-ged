package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.dto.DocumentVersionResponse;
import br.com.uds.tools.ged.repository.DocumentRepository;
import br.com.uds.tools.ged.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListVersionsDocumentService extends AbstractDocumentService {

    private final DocumentRepository documentRepository;

    public List<DocumentVersionResponse> listVersions(Long documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + documentId));
        UserPrincipal principal = currentUser();
        if (doc.getStatus() == DocumentStatus.DRAFT && !doc.getOwner().getId().equals(principal.getId())) {
            throw new IllegalArgumentException("Acesso negado");
        }
        return doc.getVersions().stream().map(this::toVersionResponse).collect(Collectors.toList());
    }
}
