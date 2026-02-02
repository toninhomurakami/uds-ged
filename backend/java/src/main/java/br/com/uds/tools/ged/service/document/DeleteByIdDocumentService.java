package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteByIdDocumentService extends AbstractDocumentService {

    private final DocumentRepository documentRepository;

    public void deleteById(Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + id));
        if (!doc.getOwner().getId().equals(currentUser().getId()) && currentUser().getRole() != br.com.uds.tools.ged.domain.Role.ADMIN) {
            throw new IllegalArgumentException("Acesso negado");
        }
        documentRepository.delete(doc);
    }

    public boolean isDeleteAllowed(Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + id));
        return doc.getOwner().getId().equals(currentUser().getId());
    }
}
