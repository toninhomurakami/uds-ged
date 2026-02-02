package br.com.uds.tools.ged.service.user;

import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetByUsernameUserService extends AbstractUserService {

    private final UserRepository userRepository;

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));
    }
}
