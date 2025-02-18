package com.gameshelf.repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.gameshelf.model.User;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User createTestUser(String suffix) {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        
        User user = new User();
        user.setUsername("testuser_" + suffix);
        user.setEmail("test_" + suffix + "@example.com");
        user.setPassword("password123");
        user.setRoles(roles);
        user.setGames(new HashSet<>());
        return user;
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void whenSaveUser_thenUserIsPersisted() {
        User testUser = createTestUser(UUID.randomUUID().toString());
        User savedUser = userRepository.save(testUser);
        
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void whenFindByUsername_thenReturnUser() {
        User testUser = createTestUser(UUID.randomUUID().toString());
        userRepository.save(testUser);
        
        Optional<User> found = userRepository.findByUsername(testUser.getUsername());
        
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(testUser.getUsername());
        assertThat(found.get().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void whenFindByEmail_thenReturnUser() {
        User testUser = createTestUser(UUID.randomUUID().toString());
        userRepository.save(testUser);
        
        Optional<User> found = userRepository.findByEmail(testUser.getEmail());
        
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(testUser.getUsername());
        assertThat(found.get().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void whenFindByNonExistentUsername_thenReturnEmpty() {
        Optional<User> found = userRepository.findByUsername("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    void whenFindByNonExistentEmail_thenReturnEmpty() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
        assertThat(found).isEmpty();
    }
}
