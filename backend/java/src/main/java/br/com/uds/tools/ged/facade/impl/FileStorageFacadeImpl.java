package br.com.uds.tools.ged.facade.impl;

import br.com.uds.tools.ged.config.StorageProperties;
import br.com.uds.tools.ged.facade.FileStorageFacade;
import br.com.uds.tools.ged.service.storage.file.DeleteByFileKeyStorageService;
import br.com.uds.tools.ged.service.storage.file.PersistFileStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

@Component
@Transactional
@RequiredArgsConstructor
public class FileStorageFacadeImpl implements FileStorageFacade {

    private final DeleteByFileKeyStorageService deleteByFileKeyStorageService;
    private final PersistFileStoreService persistFileStoreService;
    private final StorageProperties storageProperties;

    @Override
    public void deleteByFileKey(String fileKey) throws IOException {
        deleteByFileKeyStorageService.deleteByFileKey(fileKey);
    }

    @Override
    public String store(Long documentId, Long versionId, MultipartFile file) throws IOException {
        return persistFileStoreService.store(documentId, versionId, file);
    }

    @Override
    public Path resolve(String fileKey) {
        return storageProperties.getBasePathAsPath().resolve(fileKey).normalize();
    }
}
