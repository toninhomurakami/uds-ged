package br.com.uds.tools.ged.service.document;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentVersion;
import br.com.uds.tools.ged.service.FileDownload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DownloadVersionDocumentServiceTest {

    @InjectMocks
    private DownloadVersionDocumentService downloadVersionDocumentService;

    @Test
    void getFileDownload_returnsFileDownloadWithContentAndFilename() {
        byte[] content = new byte[]{1, 2, 3};
        Document doc = new Document();
        doc.setId(1L);
        DocumentVersion version = new DocumentVersion();
        version.setId(10L);
        FileDownload result = downloadVersionDocumentService.getFileDownload(doc, version, content);
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.filename()).isNotBlank();
    }

    @Test
    void getDocumentVersion_whenVersionExists_returnsVersion() {
        Document doc = new Document();
        doc.setId(1L);
        DocumentVersion v = new DocumentVersion();
        v.setId(10L);
        doc.getVersions().add(v);
        DocumentVersion result = downloadVersionDocumentService.getDocumentVersion(doc, 10L);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void getDocumentVersion_whenVersionNotFound_throws() {
        Document doc = new Document();
        doc.setId(1L);
        assertThatThrownBy(() -> downloadVersionDocumentService.getDocumentVersion(doc, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Versão não encontrada");
    }
}
