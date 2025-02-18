package com.gameshelf.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gameshelf.model.User;
import com.gameshelf.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
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
        // Remove all default stubs to avoid unnecessary stubbing errors
    }

    @Test
    void constructor_shouldThrowException_whenNullDependencies() {
        assertThatThrownBy(() -> new AuthService(null, passwordEncoder))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("UserRepository cannot be null");

        assertThatThrownBy(() -> new AuthService(userRepository, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("PasswordEncoder cannot be null");
    }

    @Test
    void registerUser_shouldSucceed_whenValidInput() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "Password123!";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        // When
        String result = authService.registerUser(username, email, password);

        // Then
        assertThat(result).isEqualTo("User registered successfully");
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
        User mockUser = new User();
        mockUser.setUsername(VALID_USERNAME);
        mockUser.setPassword(ENCODED_PASSWORD);
        
        when(userRepository.findByUsername(VALID_USERNAME)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(VALID_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // Act & Assert
        assertThat(authService.authenticateUser(VALID_USERNAME, VALID_PASSWORD)).isTrue();
    }

    @Test
    void authenticateUser_shouldSucceed_withEmail() {
        // Arrange
        User mockUser = new User();
        mockUser.setEmail(VALID_EMAIL.toLowerCase());
        mockUser.setPassword(ENCODED_PASSWORD);
        
        when(userRepository.findByUsername(VALID_EMAIL)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(VALID_EMAIL.toLowerCase())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(VALID_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // Act & Assert
        assertThat(authService.authenticateUser(VALID_EMAIL, VALID_PASSWORD)).isTrue();
    }

    @Test
    void authenticateUser_shouldFail_whenInvalidPassword() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername(VALID_USERNAME);
        mockUser.setPassword(ENCODED_PASSWORD);
        
        lenient().when(userRepository.findByUsername(VALID_USERNAME)).thenReturn(Optional.of(mockUser));
        lenient().when(passwordEncoder.matches(any(), any())).thenReturn(false);

        // Act & Assert
        boolean result = authService.authenticateUser(VALID_USERNAME, VALID_PASSWORD);
        assertThat(result).isFalse();
    }

    @Test
    void authenticateUser_shouldFail_whenUserNotFound() {
        // Arrange
        lenient().when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        lenient().when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        // Act & Assert
        boolean result = authService.authenticateUser("nonexistent", VALID_PASSWORD);
        assertThat(result).isFalse();
    }

    @Test
    void authenticateUser_shouldHandleNullInput() {
        assertThat(authService.authenticateUser(null, VALID_PASSWORD)).isFalse();
        assertThat(authService.authenticateUser(VALID_USERNAME, null)).isFalse();
    }

    @Test
    void userExists_shouldCheckUsername() {
        // Arrange
        when(userRepository.findByUsername(VALID_USERNAME))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(new User()));

        // First check
        assertThat(authService.userExists(VALID_USERNAME)).isFalse();

        // Second check
        assertThat(authService.userExists(VALID_USERNAME)).isTrue();

        verify(userRepository, times(2)).findByUsername(VALID_USERNAME);
    }
}
