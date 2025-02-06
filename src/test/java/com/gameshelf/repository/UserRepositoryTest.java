package com.gameshelf.repository;

import com.gameshelf.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveUser() {
        // Arrange
        User user = new User(null, "testuser", "test@example.com", "password", Set.of("USER"));

        // Act

        User savedUser = userRepository.save(user);

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(userRepository.findByUsername("testuser")).isPresent();
        assertThat(userRepository.findByEmail("test@example.com")).isPresent();
    }

    @Test
    void testFindByUsername() {
        // Arrange
        User user = new User(null, "findtestuser", "test@example.com", "password", Set.of("USER"));
        userRepository.save(user);
        // Act
        Optional<User> foundUser = userRepository.findByUsername("findtestuser");
        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("findtestuser");
    }
    @Test
    void testFindByEmail() {
        // Arrange
        User user = new User(null, "findtestuser", "test@example.com", "password", Set.of("USER"));
        userRepository.save(user);
        // Act
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        // Assert
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }
}
