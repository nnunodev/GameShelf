package com.gameshelf.service;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "Password123";
    private static final String ENCODED_PASSWORD = "encodedPassword123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void constructor_shouldThrowException_whenNullDependencies() {
        assertThatThrownBy(() -> new AuthService(null, passwordEncoder))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> new AuthService(userRepository, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void registerUser_shouldSucceed_whenValidInput() {
        // Arrange
        when(userRepository.findByUsername(VALID_USERNAME)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        // Act
        String result = authService.registerUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD);

        // Assert
        assertThat(result).isEqualTo("User registered successfully");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_shouldValidatePasswordComplexity() {
        assertThatThrownBy(() -> authService.registerUser(VALID_USERNAME, VALID_EMAIL, "short"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("at least 8 characters");

        assertThatThrownBy(() -> authService.registerUser(VALID_USERNAME, VALID_EMAIL, "password123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("uppercase letter");

        assertThatThrownBy(() -> authService.registerUser(VALID_USERNAME, VALID_EMAIL, "PASSWORD123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("lowercase letter");

        assertThatThrownBy(() -> authService.registerUser(VALID_USERNAME, VALID_EMAIL, "Passwordabc"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("digit");
    }

    @Test
    void registerUser_shouldValidateEmail() {
        assertThatThrownBy(() -> authService.registerUser(VALID_USERNAME, "invalid-email", VALID_PASSWORD))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid email format");
    }

    @Test
    void authenticateUser_shouldSucceed_withUsernameAndPassword() {
        // Arrange
        User user = new User(VALID_USERNAME, VALID_EMAIL, ENCODED_PASSWORD, Set.of("USER"));
        when(userRepository.findByUsername(VALID_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(VALID_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // Act & Assert
        assertThat(authService.authenticateUser(VALID_USERNAME, VALID_PASSWORD)).isTrue();
    }

    @Test
    void authenticateUser_shouldSucceed_withEmail() {
        // Arrange
        User user = new User(VALID_USERNAME, VALID_EMAIL, ENCODED_PASSWORD, Set.of("USER"));
        when(userRepository.findByUsername(VALID_EMAIL)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(VALID_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // Act & Assert
        assertThat(authService.authenticateUser(VALID_EMAIL, VALID_PASSWORD)).isTrue();
    }

    @Test
    void authenticateUser_shouldFail_whenInvalidPassword() {
        // Arrange
        User user = new User(VALID_USERNAME, VALID_EMAIL, ENCODED_PASSWORD, Set.of("USER"));
        when(userRepository.findByUsername(VALID_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", ENCODED_PASSWORD)).thenReturn(false);

        // Act & Assert
        assertThat(authService.authenticateUser(VALID_USERNAME, "wrongpassword")).isFalse();
    }

    @Test
    void authenticateUser_shouldFail_whenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThat(authService.authenticateUser("nonexistent", VALID_PASSWORD)).isFalse();
    }

    @Test
    void authenticateUser_shouldHandleNullInput() {
        assertThat(authService.authenticateUser(null, VALID_PASSWORD)).isFalse();
        assertThat(authService.authenticateUser(VALID_USERNAME, null)).isFalse();
    }

    @Test
    void userExists_shouldCheckUsername() {
        when(userRepository.findByUsername(VALID_USERNAME)).thenReturn(Optional.empty());
        assertThat(authService.userExists(VALID_USERNAME)).isFalse();

        User user = new User(VALID_USERNAME, VALID_EMAIL, ENCODED_PASSWORD, Set.of("USER"));
        when(userRepository.findByUsername(VALID_USERNAME)).thenReturn(Optional.of(user));
        assertThat(authService.userExists(VALID_USERNAME)).isTrue();
    }
}
