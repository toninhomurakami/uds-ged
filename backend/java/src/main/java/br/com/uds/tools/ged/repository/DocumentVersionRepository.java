package br.com.uds.tools.ged.repository;

import br.com.uds.tools.ged.domain.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    List<DocumentVersion> findByUploadedById(Long uploadedById);
}
