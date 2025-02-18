package com.gameshelf.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gameshelf.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);
        
        return userRepository.findByUsername(username)
            .map(user -> {
                log.debug("Found user: {}, password: {}", user.getUsername(), user.getPassword());
                return user;
            })
            .orElseThrow(() -> {
                log.error("User not found: {}", username);
                return new UsernameNotFoundException("User not found: " + username);
            });
    }
}
