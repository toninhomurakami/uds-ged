package br.com.uds.tools.ged.service.user;

import br.com.uds.tools.ged.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CountUserService extends AbstractUserService {

    private final UserRepository userRepository;

    public long count() {
        return userRepository.count();
    }
}
