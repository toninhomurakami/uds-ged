package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentVersion;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.dto.DocumentVersionResponse;
import br.com.uds.tools.ged.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;

import static br.com.uds.tools.ged.service.storage.file.AbstractStorageService.filenameFromFileKey;

public abstract class AbstractDocumentService {

    protected UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected DocumentResponse toResponse(Document doc) {
        return new DocumentResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getDescription(),
                doc.getTags() != null ? new ArrayList<>(doc.getTags()) : new ArrayList<>(),
                doc.getOwner().getId(),
                doc.getOwner().getName(),
                doc.getStatus(),
                doc.getCreatedAt(),
                doc.getUpdatedAt(),
                !doc.getVersions().isEmpty()
        );
    }

    protected DocumentVersionResponse toVersionResponse(DocumentVersion v) {
        return new DocumentVersionResponse(
                v.getId(),
                v.getFileKey(),
                v.getUploadedAt(),
                v.getUploadedBy().getId(),
                v.getUploadedBy().getName()
        );
    }


    protected static String buildDownloadFilename(DocumentVersion version, long documentId) {
        String fromKey = filenameFromFileKey(version.getFileKey());
        if (fromKey != null && !fromKey.isBlank() && !"pending".equalsIgnoreCase(fromKey)) {
            return sanitizeForDisposition(fromKey);
        }
        String ext = extensionFromFileKey(version.getFileKey());
        return "document-" + documentId + (version.getId() != null ? "-v" + version.getId() : "") + ext;
    }



    /** Sanitiza nome para Content-Disposition (evita quebra de header). */
    protected static String sanitizeForDisposition(String name) {
        if (name == null || name.isBlank()) return "download";
        String base = name.replaceAll(".*[/\\\\]", "").trim();
        base = base.replaceAll("[\\x00\\r\\n\\\\/:*?\"<>|]", "_");
        if (base.isBlank()) return "download";
        return base.length() > 255 ? base.substring(0, 255) : base;
    }


    protected static String extensionFromFileKey(String fileKey) {
        if (fileKey == null) return "";
        int i = fileKey.lastIndexOf('.');
        return i > 0 ? fileKey.substring(i).toLowerCase() : "";
    }
}
