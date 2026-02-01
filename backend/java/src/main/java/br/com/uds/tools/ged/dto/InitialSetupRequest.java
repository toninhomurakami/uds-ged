package br.com.uds.tools.ged.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InitialSetupRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 30)
    private String name;

    @NotBlank(message = "Login é obrigatório")
    @Size(max = 15)
    private String username;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 10, message = "Senha deve ter entre 6 e 10 caracteres")
    private String password;
}
