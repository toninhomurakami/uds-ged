package br.com.uds.tools.ged.dto;

import br.com.uds.tools.ged.domain.DocumentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DocumentRequest {

    @NotBlank
    @Size(max = 50)
    private String title;

    @Size(max = 100)
    private String description;

    @Size(max = 25, message = "Cada tag deve ter no máximo 25 caracteres")
    private List<@Size(max = 25) String> tags;

    private DocumentStatus status;
}
