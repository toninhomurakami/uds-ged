package br.com.uds.tools.ged.service.storage.file;

import br.com.uds.tools.ged.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DeleteByFileKeyStorageService extends AbstractStorageService {

    protected final StorageProperties storageProperties;

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

    private Path resolve(String fileKey) {
        return storageProperties.getBasePathAsPath().resolve(fileKey).normalize();
    }
}
