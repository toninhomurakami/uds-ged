package br.com.uds.tools.ged.facade;

import br.com.uds.tools.ged.config.StorageProperties;
import br.com.uds.tools.ged.facade.impl.FileStorageFacadeImpl;
import br.com.uds.tools.ged.service.storage.file.DeleteByFileKeyStorageService;
import br.com.uds.tools.ged.service.storage.file.PersistFileStoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStorageFacadeImplTest {

    @TempDir
    Path tempDir;

    @Mock
    private DeleteByFileKeyStorageService deleteByFileKeyStorageService;

    @Mock
    private PersistFileStoreService persistFileStoreService;

    @Mock
    private StorageProperties storageProperties;

    @InjectMocks
    private FileStorageFacadeImpl fileStorageFacade;

    @Test
    void deleteByFileKey_delegatesToService() throws IOException {
        fileStorageFacade.deleteByFileKey("1/1/file.pdf");
        verify(deleteByFileKeyStorageService).deleteByFileKey("1/1/file.pdf");
    }

    @Test
    void store_delegatesToServiceAndReturnsFileKey() throws IOException {
        when(persistFileStoreService.store(1L, 10L, null)).thenReturn("1/10/file.pdf");
        String fileKey = fileStorageFacade.store(1L, 10L, null);
        assertThat(fileKey).isEqualTo("1/10/file.pdf");
        verify(persistFileStoreService).store(1L, 10L, null);
    }

    @Test
    void resolve_returnsPathFromProperties() {
        Path basePath = tempDir.resolve("storage");
        when(storageProperties.getBasePathAsPath()).thenReturn(basePath);
        Path result = fileStorageFacade.resolve("1/1/file.pdf");
        assertThat(result).isEqualTo(basePath.resolve("1/1/file.pdf").normalize());
        verify(storageProperties).getBasePathAsPath();
    }
}
