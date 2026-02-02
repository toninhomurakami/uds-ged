package br.com.uds.tools.ged.service.user;

import br.com.uds.tools.ged.domain.User;
import br.com.uds.tools.ged.dto.UserResponse;

public abstract class AbstractUserService {

    protected UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getRole());
    }
}
