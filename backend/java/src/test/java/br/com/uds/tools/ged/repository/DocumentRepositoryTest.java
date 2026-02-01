package br.com.uds.tools.ged.repository;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DocumentRepository}. Uses H2 and Flyway migrations from main resources.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_shouldPersistDocumentWithOwner() {
        User owner = saveUser("owner1", Role.USER);
        Document doc = new Document();
        doc.setTitle("Test Doc");
        doc.setDescription("Desc");
        doc.setOwner(owner);
        doc.setStatus(DocumentStatus.DRAFT);

        Document saved = documentRepository.save(doc);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Test Doc");
        assertThat(saved.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(saved.getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void findByOwnerId_shouldReturnDocumentsOfOwner() {
        User owner = saveUser("owner2", Role.USER);
        Document d1 = saveDocument("Doc 1", owner, DocumentStatus.DRAFT);
        Document d2 = saveDocument("Doc 2", owner, DocumentStatus.PUBLISHED);

        List<Document> list = documentRepository.findByOwnerId(owner.getId());

        assertThat(list).hasSize(2);
        assertThat(list).extracting(Document::getTitle).containsExactlyInAnyOrder("Doc 1", "Doc 2");
    }

    @Test
    void findAllFiltered_shouldFilterByTitleAndStatus() {
        User owner = saveUser("owner3", Role.USER);
        saveDocument("Relatório Anual", owner, DocumentStatus.PUBLISHED);
        saveDocument("Relatório Mensal", owner, DocumentStatus.PUBLISHED);
        saveDocument("Outro Doc", owner, DocumentStatus.PUBLISHED);

        Pageable page = PageRequest.of(0, 10);
        Page<Document> result = documentRepository.findAllFiltered("Relatório", DocumentStatus.PUBLISHED, owner.getId(), page);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(d -> d.getTitle().toLowerCase().contains("relatório"));
    }

    @Test
    void findAllFiltered_shouldExcludeOtherUsersDrafts() {
        User owner1 = saveUser("u1", Role.USER);
        User owner2 = saveUser("u2", Role.USER);
        saveDocument("Draft do U1", owner1, DocumentStatus.DRAFT);
        saveDocument("Draft do U2", owner2, DocumentStatus.DRAFT);

        Pageable page = PageRequest.of(0, 10);
        Page<Document> asUser1 = documentRepository.findAllFiltered(null, null, owner1.getId(), page);

        // As user 1: see own DRAFT and all PUBLISHED/ARCHIVED; other users' DRAFT must not appear
        List<Document> content = asUser1.getContent();
        assertThat(content).extracting(Document::getTitle).contains("Draft do U1");
        assertThat(content).extracting(Document::getTitle).doesNotContain("Draft do U2");
    }

    private User saveUser(String username, Role role) {
        User u = new User();
        u.setName("User " + username);
        u.setUsername(username);
        u.setPassword("p");
        u.setRole(role);
        return userRepository.saveAndFlush(u);
    }

    private Document saveDocument(String title, User owner, DocumentStatus status) {
        Document d = new Document();
        d.setTitle(title);
        d.setOwner(owner);
        d.setStatus(status);
        return documentRepository.saveAndFlush(d);
    }
}
