package com.gameshelf.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gameshelf.model.User;
import com.gameshelf.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        if (userRepository == null || passwordEncoder == null) {
            throw new IllegalArgumentException("Dependencies cannot be null");
        }
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user in the system.
     * @param username the desired username
     * @param email the user's email address
     * @param password the user's password
     * @return success message
     * @throws IllegalArgumentException if any validation fails
     */
    public String registerUser(String username, String email, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        validatePassword(password);

        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, email, encodedPassword, Set.of("USER"));
        userRepository.save(user);

        return "User registered successfully";
    }

    /**
     * Authenticates a user by username/email and password.
     * @param usernameOrEmail the username or email
     * @param password the password to verify
     * @return true if authentication successful, false otherwise
     */
    public boolean authenticateUser(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || password == null) {
            return false;
        }

        Optional<User> user = userRepository.findByUsername(usernameOrEmail);
        if (user.isEmpty()) {
            user = userRepository.findByEmail(usernameOrEmail);
        }

        return user.map(u -> passwordEncoder.matches(password, u.getPassword()))
                  .orElse(false);
    }

    /**
     * Validates password complexity requirements.
     * @param password the password to validate
     * @throws IllegalArgumentException if password is invalid
     */
    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

}