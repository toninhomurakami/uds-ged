package br.com.uds.tools.ged.dto;

import br.com.uds.tools.ged.domain.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private String title;
    private String description;
    private List<String> tags;
    private Long ownerId;
    private String ownerName;
    private DocumentStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean hasCurrentVersion;
}
