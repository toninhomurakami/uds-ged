package br.com.uds.tools.ged.storage;

import br.com.uds.tools.ged.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final String[] ALLOWED_EXTENSIONS = {".pdf", ".png", ".jpg", ".jpeg"};

    private final StorageProperties storageProperties;

    /**
     * Grava o arquivo com fileKey = documentId/versionId/nome do arquivo.
     * O versionId deve ser o ID da versão já persistida.
     */
    public String store(Long documentId, Long versionId, MultipartFile file) throws IOException {
        String uploadedName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "file";
        String extension = getExtension(uploadedName);
        validateExtension(extension);
        String baseName = sanitizeForFileKey(uploadedName.substring(0, uploadedName.length() - extension.length()).trim());
        if (baseName.isEmpty()) baseName = "file";

        Path basePath = storageProperties.getBasePathAsPath();
        Files.createDirectories(basePath);

        String fileName = baseName + extension;
        String fileKey = documentId + "/" + versionId + "/" + fileName;
        Path targetPath = basePath.resolve(fileKey);
        Files.createDirectories(targetPath.getParent());
        int suffix = 0;
        while (Files.exists(targetPath)) {
            suffix++;
            fileName = baseName + "_" + suffix + extension;
            fileKey = documentId + "/" + versionId + "/" + fileName;
            targetPath = basePath.resolve(fileKey);
        }

        try (InputStream is = file.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return fileKey;
    }

    /** Nome do arquivo para download: parte do fileKey após a última barra. */
    public static String filenameFromFileKey(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) return "download";
        int last = fileKey.lastIndexOf('/');
        return last >= 0 && last < fileKey.length() - 1 ? fileKey.substring(last + 1) : fileKey;
    }

    private static String sanitizeForFileKey(String name) {
        if (name == null || name.isBlank()) return "";
        String s = name.replaceAll(".*[/\\\\]", "").trim();
        s = s.replaceAll("[\\x00\\\\/:*?\"<>|]", "_");
        return s.length() > 220 ? s.substring(0, 220) : s;
    }

    public Path resolve(String fileKey) {
        return storageProperties.getBasePathAsPath().resolve(fileKey).normalize();
    }

    /**
     * Remove o arquivo físico referenciado pelo fileKey, se existir.
     */
    public void deleteByFileKey(String fileKey) throws IOException {
        if (fileKey == null || fileKey.isBlank()) return;
        Path path = resolve(fileKey);
        if (Files.exists(path) && Files.isRegularFile(path)) {
            Files.delete(path);
        }
    }

    private static String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        return i > 0 ? filename.substring(i).toLowerCase() : "";
    }

    private static void validateExtension(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) return;
        }
        throw new IllegalArgumentException("Formato não permitido. Use PDF, PNG ou JPG.");
    }
}
