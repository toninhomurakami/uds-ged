package br.com.uds.tools.ged.web.controller.document;

import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.dto.DocumentVersionResponse;
import br.com.uds.tools.ged.dto.PageResponse;
import br.com.uds.tools.ged.facade.DocumentFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class ListDocumentController {

    private final DocumentFacade documentFacade;

    @GetMapping
    public ResponseEntity<PageResponse<DocumentResponse>> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, parseSort(sort));
        return ResponseEntity.ok(documentFacade.findAll(title, status, pageable));
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<DocumentVersionResponse>> listVersions(@PathVariable Long id) {
        return ResponseEntity.ok(documentFacade.listVersions(id));
    }

    private static Sort parseSort(String sort) {
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",", 2);
            String property = parts[0].trim();
            boolean desc = parts.length == 2 && "desc".equalsIgnoreCase(parts[1].trim());
            return Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, property);
        }
        return Sort.by(Sort.Direction.DESC, "updatedAt");
    }
}
