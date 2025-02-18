package com.gameshelf.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gameshelf.dto.AuthRequest;
import com.gameshelf.dto.LoginRequest;
import com.gameshelf.dto.TokenResponse;
import com.gameshelf.security.JwtUtil;
import com.gameshelf.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        String result = authService.registerUser(
            request.getUsername(), 
            request.getEmail(), 
            request.getPassword()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.debug("Login attempt for user: {}", request.getUsername());
            System.out.println("Debug - Login attempt:");
            System.out.println("Username: " + request.getUsername());
            System.out.println("Password length: " + (request.getPassword() != null ? request.getPassword().length() : "null"));
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            String jwt = jwtUtil.generateToken(authentication.getName());
            log.debug("Successfully generated token for user: {}", request.getUsername());
            
            return ResponseEntity.ok(new TokenResponse(jwt));
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", request.getUsername(), e);
            System.out.println("Debug - Authentication failed:");
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Error type: " + e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
