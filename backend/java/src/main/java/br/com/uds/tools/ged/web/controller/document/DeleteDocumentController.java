package br.com.uds.tools.ged.web.controller.document;

import br.com.uds.tools.ged.facade.DocumentFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DeleteDocumentController {

    private final DocumentFacade documentFacade;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IOException {
        documentFacade.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
