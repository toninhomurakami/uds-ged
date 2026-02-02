package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArchiveDocumentService extends AbstractDocumentService {

    private final DocumentRepository documentRepository;

    public DocumentResponse archive(Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + id));
        if (doc.getOwner().getId().equals(currentUser().getId()) || currentUser().getRole() == br.com.uds.tools.ged.domain.Role.ADMIN) {
            doc.setStatus(DocumentStatus.ARCHIVED);
            doc = documentRepository.save(doc);
            return toResponse(doc);
        }
        throw new IllegalArgumentException("Acesso negado");
    }
}
