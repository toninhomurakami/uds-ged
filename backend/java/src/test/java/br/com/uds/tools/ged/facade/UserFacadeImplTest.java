package br.com.uds.tools.ged.facade;

import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.dto.UserRequest;
import br.com.uds.tools.ged.dto.UserResponse;
import br.com.uds.tools.ged.facade.impl.UserFacadeImpl;
import br.com.uds.tools.ged.service.user.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFacadeImplTest {

    @Mock
    private FindAllUserService findAllUserService;

    @Mock
    private FindByIdUserService findByIdUserService;

    @Mock
    private CountUserService countUserService;

    @Mock
    private CreateInitialAdminUserService createInitialAdminUserService;

    @Mock
    private CreateUserService createUserService;

    @Mock
    private UpdateUserService updateUserService;

    @Mock
    private DeleteByIdUserService deleteByIdUserService;

    @InjectMocks
    private UserFacadeImpl userFacade;

    @Test
    void findAll_delegatesToService() {
        List<UserResponse> list = List.of(new UserResponse(1L, "Admin", "admin", Role.ADMIN));
        when(findAllUserService.findAll()).thenReturn(list);

        assertThat(userFacade.findAll()).isEqualTo(list);
        verify(findAllUserService).findAll();
    }

    @Test
    void findById_delegatesToService() {
        UserResponse user = new UserResponse(1L, "Admin", "admin", Role.ADMIN);
        when(findByIdUserService.findById(1L)).thenReturn(user);

        assertThat(userFacade.findById(1L)).isEqualTo(user);
        verify(findByIdUserService).findById(1L);
    }

    @Test
    void count_delegatesToService() {
        when(countUserService.count()).thenReturn(5L);
        assertThat(userFacade.count()).isEqualTo(5L);
        verify(countUserService).count();
    }

    @Test
    void createInitialAdmin_delegatesToService() {
        UserResponse created = new UserResponse(1L, "Admin", "admin", Role.ADMIN);
        when(createInitialAdminUserService.createInitialAdmin("Admin", "admin", "pass")).thenReturn(created);

        assertThat(userFacade.createInitialAdmin("Admin", "admin", "pass")).isEqualTo(created);
        verify(createInitialAdminUserService).createInitialAdmin("Admin", "admin", "pass");
    }

    @Test
    void create_delegatesToService() {
        UserRequest request = new UserRequest();
        request.setName("John");
        request.setUsername("john");
        request.setRole(Role.USER);
        UserResponse created = new UserResponse(1L, "John", "john", Role.USER);
        when(createUserService.create(any(UserRequest.class))).thenReturn(created);

        assertThat(userFacade.create(request)).isEqualTo(created);
        verify(createUserService).create(request);
    }

    @Test
    void update_delegatesToService() {
        UserRequest request = new UserRequest();
        request.setName("New");
        request.setUsername("user1");
        request.setRole(Role.USER);
        UserResponse updated = new UserResponse(1L, "New", "user1", Role.USER);
        when(updateUserService.update(eq(1L), any(UserRequest.class))).thenReturn(updated);

        assertThat(userFacade.update(1L, request)).isEqualTo(updated);
        verify(updateUserService).update(1L, request);
    }

    @Test
    void deleteById_delegatesToService() {
        userFacade.deleteById(1L);
        verify(deleteByIdUserService).deleteById(1L);
    }
}
