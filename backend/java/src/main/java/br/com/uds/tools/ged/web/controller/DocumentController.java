package br.com.uds.tools.ged.web.controller;

import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.service.DocumentService;
import br.com.uds.tools.ged.dto.DocumentRequest;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.dto.DocumentVersionResponse;
import br.com.uds.tools.ged.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<PageResponse<DocumentResponse>> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, parseSort(sort));
        return ResponseEntity.ok(documentService.findAll(title, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> create(@Valid @RequestBody DocumentRequest request) {
        DocumentResponse created = documentService.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> update(@PathVariable Long id, @Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.ok(documentService.update(id, request));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<DocumentResponse> publish(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.publish(id));
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<DocumentResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.archive(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IOException {
        documentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<DocumentVersionResponse>> listVersions(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.listVersions(id));
    }

    @PostMapping(value = "/{id}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentVersionResponse> uploadVersion(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        DocumentVersionResponse version = documentService.uploadVersion(id, file);
        return ResponseEntity.status(201).body(version);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadCurrent(@PathVariable Long id) throws IOException {
        var download = documentService.downloadCurrent(id);
        MediaType contentType = mediaTypeFromFilename(download.filename());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + escapeDispositionFilename(download.filename()) + "\"")
                .contentType(contentType)
                .body(download.content());
    }

    @GetMapping("/{id}/versions/{versionId}/download")
    public ResponseEntity<byte[]> downloadVersion(
            @PathVariable Long id,
            @PathVariable Long versionId
    ) throws IOException {
        var download = documentService.downloadVersion(id, versionId);
        MediaType contentType = mediaTypeFromFilename(download.filename());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + escapeDispositionFilename(download.filename()) + "\"")
                .contentType(contentType)
                .body(download.content());
    }

    private static String escapeDispositionFilename(String name) {
        if (name == null) return "download";
        return name.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** Define Content-Type pela extensão do arquivo para o navegador salvar com a extensão correta (PDF, PNG, JPG). */
    private static MediaType mediaTypeFromFilename(String filename) {
        if (filename == null) return MediaType.APPLICATION_OCTET_STREAM;
        int i = filename.lastIndexOf('.');
        if (i <= 0 || i >= filename.length() - 1) return MediaType.APPLICATION_OCTET_STREAM;
        String ext = filename.substring(i + 1).toLowerCase();
        return switch (ext) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
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
