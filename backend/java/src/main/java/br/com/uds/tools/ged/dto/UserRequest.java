package br.com.uds.tools.ged.dto;

import br.com.uds.tools.ged.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRequest {

    @NotBlank
    @Size(max = 30)
    private String name;

    @NotBlank
    @Size(max = 15)
    private String username;

    @Size(max = 10, message = "Senha deve ter no máximo 10 caracteres")
    private String password;

    @NotNull
    private Role role;
}
