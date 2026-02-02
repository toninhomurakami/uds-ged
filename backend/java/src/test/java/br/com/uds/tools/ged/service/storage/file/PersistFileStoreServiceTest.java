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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PersistFileStoreServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private StorageProperties storageProperties;

    @InjectMocks
    private PersistFileStoreService persistFileStoreService;

    @Test
    void store_whenValidPdf_returnsFileKey() throws IOException {
        Path basePath = tempDir.resolve("store");
        Files.createDirectories(basePath);
        when(storageProperties.getBasePathAsPath()).thenReturn(basePath);
        MultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

        String fileKey = persistFileStoreService.store(1L, 1L, file);

        assertThat(fileKey).isNotBlank();
        assertThat(fileKey).contains("1/1/");
        assertThat(fileKey).endsWith(".pdf");
        assertThat(Files.exists(basePath.resolve(fileKey))).isTrue();
    }

    @Test
    void store_whenInvalidExtension_throws() throws IOException {
        Path basePath = tempDir.resolve("store2");
        Files.createDirectories(basePath);
        when(storageProperties.getBasePathAsPath()).thenReturn(basePath);
        MultipartFile file = new MockMultipartFile("file", "doc.exe", "application/octet-stream", new byte[]{1});

        assertThatThrownBy(() -> persistFileStoreService.store(1L, 1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Formato não permitido");
    }
}
