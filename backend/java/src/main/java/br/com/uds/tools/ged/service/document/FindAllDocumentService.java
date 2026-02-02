package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.dto.PageResponse;
import br.com.uds.tools.ged.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FindAllDocumentService extends AbstractDocumentService {

    private final DocumentRepository documentRepository;

    public PageResponse<DocumentResponse> findAll(String title, DocumentStatus status, Pageable pageable) {
        Long userId = currentUser().getId();
        Page<Document> page = documentRepository.findAllFiltered(title, status, userId, pageable);
        List<DocumentResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

}
