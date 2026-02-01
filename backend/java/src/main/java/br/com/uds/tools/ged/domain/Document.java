package br.com.uds.tools.ged.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    @Size(max = 50)
    private String title;

    @Column(length = 100)
    @Size(max = 100)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "document_tags", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "tag", length = 25)
    @Size(max = 25)
    private List<String> tags = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("uploadedAt DESC")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DocumentVersion> versions = new ArrayList<>();

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

}
