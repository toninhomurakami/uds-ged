package br.com.uds.tools.ged.web.controller.document;

import br.com.uds.tools.ged.dto.DocumentVersionResponse;
import br.com.uds.tools.ged.facade.DocumentFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class UploadDocumentController {

    private final DocumentFacade documentFacade;

    @PostMapping(value = "/{id}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentVersionResponse> uploadVersion(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        DocumentVersionResponse version = documentFacade.uploadVersion(id, file);
        return ResponseEntity.status(201).body(version);
    }
}
