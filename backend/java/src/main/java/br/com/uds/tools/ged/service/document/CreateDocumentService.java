package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.dto.DocumentRequest;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.repository.DocumentRepository;
import br.com.uds.tools.ged.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateDocumentService extends AbstractDocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public DocumentResponse create(DocumentRequest request) {
        User owner = userRepository.findById(currentUser().getId())
                .orElseThrow();
        List<String> tags = request.getTags() != null ? new ArrayList<>(request.getTags()) : new ArrayList<>();
        DocumentStatus status = request.getStatus() != null ? request.getStatus() : DocumentStatus.DRAFT;

        Document doc = new Document();
        doc.setTitle(request.getTitle());
        doc.setDescription(request.getDescription());
        doc.setTags(tags);
        doc.setOwner(owner);
        doc.setStatus(status);
        doc.setCreatedAt(java.time.Instant.now());
        doc.setUpdatedAt(java.time.Instant.now());
        doc = documentRepository.save(doc);
        return toResponse(doc);
    }
}
