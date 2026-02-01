package br.com.uds.tools.ged.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "document_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Document document;

    @Column(name = "file_key", nullable = false, length = 512)
    private String fileKey;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User uploadedBy;
}
