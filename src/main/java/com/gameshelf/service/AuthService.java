package com.gameshelf.service;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gameshelf.model.User;
import com.gameshelf.repository.UserRepository;

/**
 * Service responsible for handling user authentication and registration operations.
 * This service provides methods for user registration, authentication, and credential validation.
 */
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Compile patterns once during initialization for better performance
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
    
    /**
     * Constructs a new AuthService with required dependencies.
     * 
     * @param userRepository Repository for user data operations
     * @param passwordEncoder Encoder for password hashing
     * @throws IllegalArgumentException if any dependency is null
     */
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
     * Registers a new user in the system.
     * 
     * @param username the desired username for the new user
     * @param email the email address for the new user
     * @param password the password for the new user
     * @return a success message if registration is successful
     * @throws IllegalArgumentException if any validation fails (username exists, invalid email, weak password)
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
     * Authenticates a user using either username or email with the provided password.
     * 
     * @param identifier the username or email of the user
     * @param password the password to verify
     * @return true if authentication is successful, false otherwise
     */
    public boolean authenticateUser(String identifier, String password) {
        if (identifier == null || password == null) {
            System.out.println("Debug: Null identifier or password");
            return false;
        }

        String trimmedIdentifier = identifier.trim();
        Optional<User> userOpt = userRepository.findByUsername(trimmedIdentifier);
        
        if (!userOpt.isPresent()) {
            trimmedIdentifier = trimmedIdentifier.toLowerCase();
            userOpt = userRepository.findByEmail(trimmedIdentifier);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("Debug: Found user: " + user.getUsername());
            System.out.println("Debug: Stored encoded password: " + user.getPassword());
            System.out.println("Debug: Attempting to match with provided password: " + password);
            boolean matches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("Debug: Password match result: " + matches);
            return matches;
        }
        System.out.println("Debug: User not found");
        return false;
    }

    /**
     * Checks if a username already exists in the system.
     * 
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    public boolean userExists(String username) {
        if (username == null) {
            return false;
        }
        return userRepository.findByUsername(username.trim()).isPresent();
    }

    private void validateRegistrationInput(String username, String email, String password) {
        if (!validateUsername(username)) {
            throw new IllegalArgumentException("Invalid username format: must be 3-20 characters, alphanumeric with - and _");
        }

        if (!validateEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        
        if (!validatePassword(password)) {
            throw new IllegalArgumentException(
                "Password must be at least 8 characters long and include: " +
                "one uppercase letter, one lowercase letter, one digit, " +
                "and one special character"
            );
        }
    }

    /**
     * Validates email format.
     * 
     * @param email the email address to validate
     * @return true if the email format is valid, false otherwise
     */
    public boolean validateEmail(String email) {
        if (email == null || email.length() > 254) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validates password complexity requirements.
     * Password must contain:
     * - At least 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     * 
     * @param password the password to validate
     * @return true if the password meets all requirements, false otherwise
     */
    public boolean validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 128) {
            return false; 
        }

        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()+=\\-_{}\\[\\]|:;\"'<>,.?/~`].*");

        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }
    
    /**
     * Validates username format requirements.
     * Username must be:
     * - 3-20 characters long
     * - Contain only alphanumeric characters, underscores, and hyphens
     * 
     * @param username the username to validate
     * @return true if the username format is valid, false otherwise
     */
    public boolean validateUsername(String username) {
        if (username == null || username.length() > 20) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }
}