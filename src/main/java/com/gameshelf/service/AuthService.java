package com.gameshelf.service;

import java.util.Optional;
import java.util.Set;

import com.gameshelf.model.User;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gameshelf.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String registerUser(String username, String email, String password) {
        // Check if the user already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Encrypt password
        String encodedPassword = passwordEncoder.encode(password);

        // Save new user

        User user = new User(username, email, encodedPassword, Set.of("USER"));
        userRepository.save(user);

        return "User registered successfully";

    }

    public boolean authenticateUser(String username, String password) {

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent())
            return passwordEncoder.matches(password, user.get().getPassword());
        else
            return false;
    }

    public boolean userExists(String username) {
        // Implement this method to check if the user exists
        return userRepository.findByUsername(username).isPresent();
    }
}
