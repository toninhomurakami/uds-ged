package br.com.uds.tools.ged.web.controller.document;

import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.facade.DocumentFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class GetDocumentController {

    private final DocumentFacade documentFacade;

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentFacade.findById(id));
    }
}
