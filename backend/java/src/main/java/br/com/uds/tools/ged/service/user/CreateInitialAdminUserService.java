package br.com.uds.tools.ged.service.user;

import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.dto.UserResponse;
import br.com.uds.tools.ged.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateInitialAdminUserService extends AbstractUserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Cria o primeiro usuário ADMIN (setup inicial). Só permite quando não existe nenhum usuário.
     */
    public UserResponse createInitialAdmin(String name, String username, String password) {
        if (userRepository.count() > 0) {
            throw new IllegalStateException("Setup inicial já foi realizado. Já existem usuários cadastrados.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Nome de usuário já existe");
        }
        User admin = new User();
        admin.setName(name != null ? name.trim() : null);
        admin.setUsername(username.trim());
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);
        return toResponse(admin);
    }
}
