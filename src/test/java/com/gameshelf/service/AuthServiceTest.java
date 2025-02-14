package com.gameshelf.service;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gameshelf.model.User;
import com.gameshelf.repository.UserRepository;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        User savedUser = new User(null, username, email, encodedPassword, Set.of("USER"));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        String result = authService.registerUser(username, email, password);

        // Assert
        assertThat(result).isEqualTo("User registered successfully");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testAuthenticateUser_Success() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String encodedPassword = "encodedPassword123";

        User user = new User(null, username, "test@example.com", encodedPassword, Set.of("USER"));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        // Act
        boolean isAuthenticated = authService.authenticateUser(username, password);

        // Assert
        assertThat(isAuthenticated).isTrue();
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testAuthenticateUser_InvalidPassword() {
        // Arrange
        String username = "testuser";
        String password = "wrongpassword";
        String encodedPassword = "encodedPassword123";

        User user = new User(null, username, "test@example.com", encodedPassword, Set.of("USER"));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        // Act
        boolean isAuthenticated = authService.authenticateUser(username, password);

        // Assert
        assertThat(isAuthenticated).isFalse();
    }

    @Test
    void testAuthenticateUser_UserNotFound() {
        // Arrange
        String username = "nonexistent";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        boolean isAuthenticated = authService.authenticateUser(username, password);

        // Assert
        assertThat(isAuthenticated).isFalse();
    }
}
