package br.com.uds.tools.ged.storage;

import br.com.uds.tools.ged.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties();
        props.setBasePath(tempDir.toString());
        fileStorageService = new FileStorageService(props);
    }

    @Test
    void filenameFromFileKey_withFullPath_returnsLastName() {
        assertThat(FileStorageService.filenameFromFileKey("1/2/relatorio.pdf")).isEqualTo("relatorio.pdf");
        assertThat(FileStorageService.filenameFromFileKey("10/20/arquivo.png")).isEqualTo("arquivo.png");
    }

    @Test
    void filenameFromFileKey_withNull_returnsDownload() {
        assertThat(FileStorageService.filenameFromFileKey(null)).isEqualTo("download");
    }

    @Test
    void filenameFromFileKey_withEmpty_returnsDownload() {
        assertThat(FileStorageService.filenameFromFileKey("")).isEqualTo("download");
    }

    @Test
    void filenameFromFileKey_withNoSlash_returnsSameKey() {
        assertThat(FileStorageService.filenameFromFileKey("single")).isEqualTo("single");
    }

    @Test
    void resolve_returnsNormalizedPath() {
        Path resolved = fileStorageService.resolve("1/2/file.pdf");
        assertThat(resolved.getFileName().toString()).isEqualTo("file.pdf");
        assertThat(resolved).isAbsolute();
        assertThat(resolved.toString()).contains("1").contains("2").contains("file.pdf");
    }

    @Test
    void store_acceptsPdfAndReturnsFileKey() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "content".getBytes());
        String fileKey = fileStorageService.store(1L, 2L, file);
        assertThat(fileKey).isEqualTo("1/2/doc.pdf");
        assertThat(Files.exists(tempDir.resolve("1/2/doc.pdf"))).isTrue();
    }

    @Test
    void store_acceptsPngAndReturnsFileKey() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "img.png", "image/png", "x".getBytes());
        String fileKey = fileStorageService.store(10L, 20L, file);
        assertThat(fileKey).isEqualTo("10/20/img.png");
        assertThat(Files.exists(tempDir.resolve("10/20/img.png"))).isTrue();
    }

    @Test
    void store_rejectsInvalidExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "script.exe", "application/octet-stream", "x".getBytes());
        assertThatThrownBy(() -> fileStorageService.store(1L, 1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Formato não permitido");
    }

    @Test
    void deleteByFileKey_whenKeyNull_doesNothing() throws IOException {
        fileStorageService.deleteByFileKey(null);
    }

    @Test
    void deleteByFileKey_whenKeyBlank_doesNothing() throws IOException {
        fileStorageService.deleteByFileKey("   ");
    }

    @Test
    void deleteByFileKey_whenFileExists_removesFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile("f", "x.pdf", "application/pdf", "a".getBytes());
        String fileKey = fileStorageService.store(1L, 1L, file);
        assertThat(Files.exists(tempDir.resolve(fileKey))).isTrue();
        fileStorageService.deleteByFileKey(fileKey);
        assertThat(Files.exists(tempDir.resolve(fileKey))).isFalse();
    }

    @Test
    void deleteByFileKey_whenFileDoesNotExist_doesNotThrow() throws IOException {
        fileStorageService.deleteByFileKey("99/99/naoexiste.pdf");
    }
}
