package br.com.uds.tools.ged.web.controller.document;

import br.com.uds.tools.ged.dto.DocumentRequest;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.facade.DocumentFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class UpdateDocumentController {

    private final DocumentFacade documentFacade;

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> update(@PathVariable Long id, @Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.ok(documentFacade.update(id, request));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<DocumentResponse> publish(@PathVariable Long id) {
        return ResponseEntity.ok(documentFacade.publish(id));
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<DocumentResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(documentFacade.archive(id));
    }
}
