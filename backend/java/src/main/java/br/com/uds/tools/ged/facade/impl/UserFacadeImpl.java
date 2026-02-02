package br.com.uds.tools.ged.facade.impl;

import br.com.uds.tools.ged.dto.UserRequest;
import br.com.uds.tools.ged.dto.UserResponse;
import br.com.uds.tools.ged.facade.UserFacade;
import br.com.uds.tools.ged.service.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {

    private final FindAllUserService findAllUserService;
    private final FindByIdUserService findByIdUserService;
    private final CountUserService countUserService;
    private final CreateInitialAdminUserService createInitialAdminUserService;
    private final CreateUserService createUserService;
    private final UpdateUserService updateUserService;
    private final DeleteByIdUserService deleteByIdUserService;

    @Override
    public List<UserResponse> findAll() {
        return findAllUserService.findAll();
    }

    @Override
    public UserResponse findById(Long id) {
        return findByIdUserService.findById(id);
    }

    @Override
    public long count() {
        return countUserService.count();
    }

    @Override
    public UserResponse createInitialAdmin(String name, String username, String password) {
        return createInitialAdminUserService.createInitialAdmin(name, username, password);
    }

    @Override
    public UserResponse create(UserRequest request) {
        return createUserService.create(request);
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {
        return updateUserService.update(id, request);
    }

    @Override
    public void deleteById(Long id) {
        deleteByIdUserService.deleteById(id);
    }
}
