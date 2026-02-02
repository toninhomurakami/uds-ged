package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.dto.DocumentRequest;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.repository.DocumentRepository;
import br.com.uds.tools.ged.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UpdateDocumentService extends AbstractDocumentService {

    private final DocumentRepository documentRepository;

    public DocumentResponse update(Long id, DocumentRequest request) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + id));
        UserPrincipal principal = currentUser();
        if (doc.getStatus() == DocumentStatus.DRAFT && !doc.getOwner().getId().equals(principal.getId())) {
            throw new IllegalArgumentException("Acesso negado a documento em rascunho");
        }
        doc.setTitle(request.getTitle());
        doc.setDescription(request.getDescription());
        if (request.getTags() != null) doc.setTags(new ArrayList<>(request.getTags()));
        if (request.getStatus() != null) doc.setStatus(request.getStatus());
        doc = documentRepository.save(doc);
        return toResponse(doc);
    }
}
