package com.gameshelf.service;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gameshelf.model.User;
import com.gameshelf.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Improved email pattern for better validation
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String PASSWORD_UPPERCASE_REGEX = ".*[A-Z].*";
    private static final String PASSWORD_LOWERCASE_REGEX = ".*[a-z].*";
    private static final String PASSWORD_DIGIT_REGEX = ".*\\d.*";

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        if (passwordEncoder == null) {
            throw new IllegalArgumentException("PasswordEncoder cannot be null");
        }
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with the provided credentials.
     * @throws IllegalArgumentException if registration validation fails
     */
    public String registerUser(String username, String email, String password) {
        validateRegistrationInput(username, email, password);
            
        String trimmedUsername = username.trim();
        String trimmedEmail = email.toLowerCase().trim();
            
        if (userRepository.findByUsername(trimmedUsername).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(trimmedEmail).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(trimmedUsername);
        user.setEmail(trimmedEmail);
        user.setPassword(passwordEncoder.encode(password));
        
        userRepository.save(user);
        return "User registered successfully";
    }

    /**
     * Authenticates a user using either username or email.
     * @return boolean indicating if authentication was successful
     */
    public boolean authenticateUser(String identifier, String password) {
        if (identifier == null || password == null) {
            return false;
        }

        String trimmedIdentifier = identifier.trim();
        Optional<User> userOpt = userRepository.findByUsername(trimmedIdentifier);
        
        if (!userOpt.isPresent()) {
            trimmedIdentifier = trimmedIdentifier.toLowerCase();
            userOpt = userRepository.findByEmail(trimmedIdentifier);
        }

        if (userOpt.isPresent()) {
            return passwordEncoder.matches(password, userOpt.get().getPassword());
        }
        return false;
    }

    public boolean userExists(String username) {
        if (username == null) {
            return false;
        }
        return userRepository.findByUsername(username.trim()).isPresent();
    }

    private void validateRegistrationInput(String username, String email, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        validatePassword(password);
    }

    private void validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        
        if (!password.matches(PASSWORD_UPPERCASE_REGEX)) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        
        if (!password.matches(PASSWORD_LOWERCASE_REGEX)) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        
        if (!password.matches(PASSWORD_DIGIT_REGEX)) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
    }
}