package br.com.uds.tools.ged.service.storage.file;

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
public class PersistFileStoreService extends AbstractStorageService {

    protected final StorageProperties storageProperties;

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

}
