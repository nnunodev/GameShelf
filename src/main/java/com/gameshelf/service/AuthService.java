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
        
        // Use a more efficient email validation pattern with bounded repetition
        if (email == null || !email.matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$")) {
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
        
        // Use simple character checks instead of regex
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        if (!hasUpper) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!hasLower) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (!hasDigit) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

}