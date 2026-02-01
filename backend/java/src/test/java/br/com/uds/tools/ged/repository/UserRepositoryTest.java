package br.com.uds.tools.ged.repository;

import br.com.uds.tools.ged.domain.Role;
import br.com.uds.tools.ged.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link UserRepository}. Uses H2 and Flyway migrations from main resources.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_shouldPersistAndReturnWithId() {
        User user = new User();
        user.setName("Admin");
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setRole(Role.ADMIN);

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("admin");
        assertThat(saved.getName()).isEqualTo("Admin");
        assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void findByUsername_shouldReturnUserWhenExists() {
        User user = new User();
        user.setName("User");
        user.setUsername("johndoe");
        user.setPassword("pass");
        user.setRole(Role.USER);
        userRepository.saveAndFlush(user);

        Optional<User> found = userRepository.findByUsername("johndoe");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("johndoe");
        assertThat(found.get().getName()).isEqualTo("User");
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenNotExists() {
        Optional<User> found = userRepository.findByUsername("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    void existsByUsername_shouldReturnTrueWhenExists() {
        User user = new User();
        user.setName("X");
        user.setUsername("existing");
        user.setPassword("p");
        user.setRole(Role.USER);
        userRepository.saveAndFlush(user);

        assertThat(userRepository.existsByUsername("existing")).isTrue();
    }

    @Test
    void existsByUsername_shouldReturnFalseWhenNotExists() {
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    void count_shouldReturnNumberOfUsers() {
        long before = userRepository.count();

        User u = new User();
        u.setName("A");
        u.setUsername("countuser");
        u.setPassword("p");
        u.setRole(Role.USER);
        userRepository.saveAndFlush(u);

        assertThat(userRepository.count()).isEqualTo(before + 1);
    }
}
