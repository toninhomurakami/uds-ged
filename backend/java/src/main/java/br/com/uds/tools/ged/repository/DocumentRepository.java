package br.com.uds.tools.ged.repository;

import br.com.uds.tools.ged.domain.Document;
import br.com.uds.tools.ged.domain.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("""
        SELECT d FROM Document d
        WHERE (:title IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (:status IS NULL OR d.status = :status)
        AND (
            d.status <> 'DRAFT'
            OR d.owner.id = :currentUserId
        )
        """)
    Page<Document> findAllFiltered(
            @Param("title") String title,
            @Param("status") DocumentStatus status,
            @Param("currentUserId") Long currentUserId,
            Pageable pageable
    );

    List<Document> findByOwnerId(Long ownerId);
}
