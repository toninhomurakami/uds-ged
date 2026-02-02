package br.com.uds.tools.ged.repository;

import br.com.uds.tools.ged.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DocumentVersionRepository}. Uses H2 and Flyway migrations from main resources.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class DocumentVersionRepositoryTest {

    @Autowired
    private DocumentVersionRepository documentVersionRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_shouldPersistVersionWithDocumentAndUploader() {
        User owner = saveUser("uploader1", Role.USER);
        Document doc = saveDocument("Doc", owner);
        DocumentVersion version = new DocumentVersion();
        version.setDocument(doc);
        version.setFileKey("1/1/file.pdf");
        version.setUploadedAt(Instant.now());
        version.setUploadedBy(owner);

        DocumentVersion saved = documentVersionRepository.save(version);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFileKey()).isEqualTo("1/1/file.pdf");
        assertThat(saved.getDocument().getId()).isEqualTo(doc.getId());
        assertThat(saved.getUploadedBy().getId()).isEqualTo(owner.getId());
    }

    @Test
    void findByUploadedById_shouldReturnVersionsUploadedByUser() {
        User u1 = saveUser("u1", Role.USER);
        User u2 = saveUser("u2", Role.USER);
        Document d1 = saveDocument("D1", u1);
        Document d2 = saveDocument("D2", u2);
        saveVersion(d1, u1, "1/1/a.pdf");
        saveVersion(d2, u2, "2/1/b.pdf");
        saveVersion(d1, u2, "1/2/c.pdf"); // u2 uploaded version to d1

        List<DocumentVersion> byU1 = documentVersionRepository.findByUploadedById(u1.getId());
        List<DocumentVersion> byU2 = documentVersionRepository.findByUploadedById(u2.getId());

        assertThat(byU1).hasSize(1);
        assertThat(byU1.get(0).getFileKey()).isEqualTo("1/1/a.pdf");
        assertThat(byU2).hasSize(2);
    }

    private User saveUser(String username, Role role) {
        User u = new User();
        u.setName("User " + username);
        u.setUsername(username);
        u.setPassword("p");
        u.setRole(role);
        return userRepository.saveAndFlush(u);
    }

    private Document saveDocument(String title, User owner) {
        Document d = new Document();
        d.setTitle(title);
        d.setOwner(owner);
        d.setStatus(DocumentStatus.DRAFT);
        return documentRepository.saveAndFlush(d);
    }

    private DocumentVersion saveVersion(Document doc, User uploadedBy, String fileKey) {
        DocumentVersion v = new DocumentVersion();
        v.setDocument(doc);
        v.setFileKey(fileKey);
        v.setUploadedAt(Instant.now());
        v.setUploadedBy(uploadedBy);
        return documentVersionRepository.saveAndFlush(v);
    }
}
