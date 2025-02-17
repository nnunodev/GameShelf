package com.gameshelf.repository;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.gameshelf.model.User;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password123", Set.of("USER"));
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void whenSaveUser_thenUserIsPersisted() {
        // Act
        User savedUser = userRepository.save(testUser);

        // Assert
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void whenFindByUsername_thenReturnUser() {
        // Arrange 
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByUsername(testUser.getUsername());

        // Assert
        assertThat(foundUser)
            .isPresent()
            .hasValueSatisfying(user -> {
                assertThat(user.getUsername()).isEqualTo(testUser.getUsername());
                assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
            });
    }

    @Test
    void whenFindByEmail_thenReturnUser() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByEmail(testUser.getEmail());

        // Assert
        assertThat(foundUser)
            .isPresent()
            .hasValueSatisfying(user -> {
                assertThat(user.getUsername()).isEqualTo(testUser.getUsername());
                assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
            });
    }

    @Test
    void whenFindByNonExistentUsername_thenReturnEmpty() {
        // Act
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    void whenFindByNonExistentEmail_thenReturnEmpty() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(foundUser).isEmpty();
    }
}
