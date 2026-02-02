package br.com.uds.tools.ged.facade;

import br.com.uds.tools.ged.dto.UserRequest;
import br.com.uds.tools.ged.dto.UserResponse;

import java.util.List;

public interface UserFacade {

    List<UserResponse> findAll();

    UserResponse findById(Long id);

    long count();

    UserResponse createInitialAdmin(String name, String username, String password);

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserRequest request);

    void deleteById(Long id);

}
