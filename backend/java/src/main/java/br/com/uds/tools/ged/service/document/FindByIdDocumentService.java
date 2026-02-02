package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.repository.DocumentRepository;
import br.com.uds.tools.ged.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindByIdDocumentService extends AbstractDocumentService {

    private final DocumentRepository documentRepository;

    public DocumentResponse findById(Long id) {
        Document doc = findDocumentById(id);
        return toResponse(doc);
    }

    public Document findDocumentById(Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + id));
        UserPrincipal principal = currentUser();
        if (doc.getStatus() == DocumentStatus.DRAFT && !doc.getOwner().getId().equals(principal.getId())) {
            throw new IllegalArgumentException("Acesso negado a documento em rascunho");
        }
        return doc;
    }
}
