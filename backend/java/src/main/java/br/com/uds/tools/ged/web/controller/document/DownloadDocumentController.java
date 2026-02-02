package br.com.uds.tools.ged.web.controller.document;

import br.com.uds.tools.ged.facade.DocumentFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DownloadDocumentController {

    private final DocumentFacade documentFacade;

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadCurrent(@PathVariable Long id) throws IOException {
        var download = documentFacade.downloadCurrent(id);
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
        var download = documentFacade.downloadVersion(id, versionId);
        MediaType contentType = mediaTypeFromFilename(download.filename());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + escapeDispositionFilename(download.filename()) + "\"")
                .contentType(contentType)
                .body(download.content());
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

    private static String escapeDispositionFilename(String name) {
        if (name == null) return "download";
        return name.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
