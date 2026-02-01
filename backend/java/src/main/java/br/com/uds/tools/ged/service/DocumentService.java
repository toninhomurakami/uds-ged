package br.com.uds.tools.ged.service;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.domain.DocumentVersion;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.repository.DocumentRepository;
import br.com.uds.tools.ged.repository.DocumentVersionRepository;
import br.com.uds.tools.ged.repository.UserRepository;
import br.com.uds.tools.ged.security.UserPrincipal;
import br.com.uds.tools.ged.storage.FileStorageService;
import br.com.uds.tools.ged.dto.DocumentRequest;
import br.com.uds.tools.ged.dto.DocumentResponse;
import br.com.uds.tools.ged.dto.DocumentVersionResponse;
import br.com.uds.tools.ged.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.uds.tools.ged.storage.FileStorageService.filenameFromFileKey;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentResponse> findAll(String title, DocumentStatus status, Pageable pageable) {
        Long userId = currentUser().getId();
        Page<Document> page = documentRepository.findAllFiltered(title, status, userId, pageable);
        List<DocumentResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public DocumentResponse findById(Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + id));
        UserPrincipal principal = currentUser();
        if (doc.getStatus() == DocumentStatus.DRAFT && !doc.getOwner().getId().equals(principal.getId())) {
            throw new IllegalArgumentException("Acesso negado a documento em rascunho");
        }
        return toResponse(doc);
    }

    @Transactional
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

    @Transactional
    public DocumentResponse update(Long id, DocumentRequest request) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + id));
        UserPrincipal principal = currentUser();
        if (doc.getStatus() == DocumentStatus.DRAFT && !doc.getOwner().getId().equals(principal.getId())) {
            throw new IllegalArgumentException("Acesso negado a documento em rascunho");
        }
        doc.setTitle(request.getTitle());
        doc.setDescription(request.getDescription());
        if (request.getTags() != null) doc.setTags(new ArrayList<>(request.getTags()));
        if (request.getStatus() != null) doc.setStatus(request.getStatus());
        doc = documentRepository.save(doc);
        return toResponse(doc);
    }

    @Transactional
    public DocumentResponse publish(Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + id));
        if (doc.getOwner().getId().equals(currentUser().getId()) || currentUser().getRole() == br.com.uds.tools.ged.domain.Role.ADMIN) {
            doc.setStatus(DocumentStatus.PUBLISHED);
            doc = documentRepository.save(doc);
            return toResponse(doc);
        }
        throw new IllegalArgumentException("Acesso negado");
    }

    @Transactional
    public DocumentResponse archive(Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + id));
        if (doc.getOwner().getId().equals(currentUser().getId()) || currentUser().getRole() == br.com.uds.tools.ged.domain.Role.ADMIN) {
            doc.setStatus(DocumentStatus.ARCHIVED);
            doc = documentRepository.save(doc);
            return toResponse(doc);
        }
        throw new IllegalArgumentException("Acesso negado");
    }

    @Transactional
    public void deleteById(Long id) throws IOException {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + id));
        if (!doc.getOwner().getId().equals(currentUser().getId()) && currentUser().getRole() != br.com.uds.tools.ged.domain.Role.ADMIN) {
            throw new IllegalArgumentException("Acesso negado");
        }
        for (DocumentVersion version : doc.getVersions()) {
            try {
                fileStorageService.deleteByFileKey(version.getFileKey());
            } catch (IOException e) {
                // continua; registros serão removidos mesmo se um arquivo falhar
            }
        }
        documentRepository.delete(doc);
    }

    @Transactional
    public DocumentVersionResponse uploadVersion(Long documentId, MultipartFile file) throws IOException {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + documentId));
        UserPrincipal principal = currentUser();
        if (doc.getStatus() == DocumentStatus.DRAFT && !doc.getOwner().getId().equals(principal.getId())) {
            throw new IllegalArgumentException("Acesso negado");
        }
        User uploadedBy = userRepository.findById(principal.getId()).orElseThrow();
        // Persiste a versão primeiro para obter o ID; depois grava o arquivo e atualiza o fileKey
        DocumentVersion version = new DocumentVersion();
        version.setDocument(doc);
        version.setFileKey(documentId + "/0/pending");
        version.setUploadedAt(java.time.Instant.now());
        version.setUploadedBy(uploadedBy);
        version = documentVersionRepository.saveAndFlush(version);
        String fileKey = fileStorageService.store(documentId, version.getId(), file);
        version.setFileKey(fileKey);
        documentVersionRepository.saveAndFlush(version);
        return toVersionResponse(version);
    }

    @Transactional(readOnly = true)
    public List<DocumentVersionResponse> listVersions(Long documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + documentId));
        UserPrincipal principal = currentUser();
        if (doc.getStatus() == DocumentStatus.DRAFT && !doc.getOwner().getId().equals(principal.getId())) {
            throw new IllegalArgumentException("Acesso negado");
        }
        return doc.getVersions().stream().map(this::toVersionResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FileDownload downloadVersion(Long documentId, Long versionId) throws IOException {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + documentId));
        UserPrincipal principal = currentUser();
        if (doc.getStatus() == DocumentStatus.DRAFT && !doc.getOwner().getId().equals(principal.getId())) {
            throw new IllegalArgumentException("Acesso negado");
        }
        DocumentVersion version = doc.getVersions().stream()
                .filter(v -> v.getId().equals(versionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Versão não encontrada"));
        Path path = fileStorageService.resolve(version.getFileKey());
        if (!Files.exists(path)) throw new IllegalArgumentException("Arquivo não encontrado no storage");
        byte[] content = Files.readAllBytes(path);
        String filename = buildDownloadFilename(version, doc.getId());
        return new FileDownload(content, filename);
    }

    @Transactional(readOnly = true)
    public FileDownload downloadCurrent(Long documentId) throws IOException {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + documentId));
        UserPrincipal principal = currentUser();
        if (doc.getStatus() == DocumentStatus.DRAFT && !doc.getOwner().getId().equals(principal.getId())) {
            throw new IllegalArgumentException("Acesso negado");
        }
        if (doc.getVersions().isEmpty()) throw new IllegalArgumentException("Documento não possui arquivo anexado");
        DocumentVersion current = doc.getVersions().stream()
                .filter(v -> {
                    Path p = fileStorageService.resolve(v.getFileKey());
                    return Files.exists(p) && Files.isRegularFile(p);
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Arquivo não encontrado no storage"));
        Path path = fileStorageService.resolve(current.getFileKey());
        byte[] content = Files.readAllBytes(path);
        String filename = buildDownloadFilename(current, doc.getId());
        return new FileDownload(content, filename);
    }

    private static String buildDownloadFilename(DocumentVersion version, long documentId) {
        String fromKey = filenameFromFileKey(version.getFileKey());
        if (fromKey != null && !fromKey.isBlank() && !"pending".equalsIgnoreCase(fromKey)) {
            return sanitizeForDisposition(fromKey);
        }
        String ext = extensionFromFileKey(version.getFileKey());
        return "document-" + documentId + (version.getId() != null ? "-v" + version.getId() : "") + ext;
    }

    private static String extensionFromFileKey(String fileKey) {
        if (fileKey == null) return "";
        int i = fileKey.lastIndexOf('.');
        return i > 0 ? fileKey.substring(i).toLowerCase() : "";
    }

    /** Sanitiza nome para Content-Disposition (evita quebra de header). */
    private static String sanitizeForDisposition(String name) {
        if (name == null || name.isBlank()) return "download";
        String base = name.replaceAll(".*[/\\\\]", "").trim();
        base = base.replaceAll("[\\x00\\r\\n\\\\/:*?\"<>|]", "_");
        if (base.isBlank()) return "download";
        return base.length() > 255 ? base.substring(0, 255) : base;
    }

    private DocumentResponse toResponse(Document doc) {
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

    private DocumentVersionResponse toVersionResponse(DocumentVersion v) {
        return new DocumentVersionResponse(
                v.getId(),
                v.getFileKey(),
                v.getUploadedAt(),
                v.getUploadedBy().getId(),
                v.getUploadedBy().getName()
        );
    }
}
