package br.com.uds.tools.ged.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersionResponse {

    private Long id;
    private String fileKey;
    private Instant uploadedAt;
    private Long uploadedById;
    private String uploadedByName;
}
