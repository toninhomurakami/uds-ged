package br.com.uds.tools.ged.service.storage.file;

import br.com.uds.tools.ged.config.StorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeleteByFileKeyStorageServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private StorageProperties storageProperties;

    @InjectMocks
    private DeleteByFileKeyStorageService deleteByFileKeyStorageService;

    @Test
    void deleteByFileKey_whenFileKeyNull_doesNothing() throws IOException {
        assertThatCode(() -> deleteByFileKeyStorageService.deleteByFileKey(null)).doesNotThrowAnyException();
    }

    @Test
    void deleteByFileKey_whenFileKeyBlank_doesNothing() throws IOException {
        when(storageProperties.getBasePathAsPath()).thenReturn(tempDir);
        assertThatCode(() -> deleteByFileKeyStorageService.deleteByFileKey("  ")).doesNotThrowAnyException();
    }

    @Test
    void deleteByFileKey_whenFileExists_deletesFile() throws IOException {
        Path basePath = tempDir.resolve("base");
        Files.createDirectories(basePath);
        Path filePath = basePath.resolve("1/1/file.pdf");
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "content");
        when(storageProperties.getBasePathAsPath()).thenReturn(basePath);

        deleteByFileKeyStorageService.deleteByFileKey("1/1/file.pdf");

        assertThat(Files.exists(filePath)).isFalse();
    }
}
