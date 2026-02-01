package br.com.uds.tools.ged.dto;

import br.com.uds.tools.ged.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String username;
    private Role role;
}
