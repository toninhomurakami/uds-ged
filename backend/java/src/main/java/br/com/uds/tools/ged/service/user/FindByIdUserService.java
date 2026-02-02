package br.com.uds.tools.ged.service.user;

import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.dto.UserResponse;
import br.com.uds.tools.ged.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindByIdUserService extends AbstractUserService {
    private final UserRepository userRepository;

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
        return toResponse(user);
    }
}
