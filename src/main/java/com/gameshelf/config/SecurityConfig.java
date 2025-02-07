package com.gameshelf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // For all other endpoints, let CSRF protection be enabled by default
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/h2-console/**", "/error").permitAll()
                        .anyRequest().authenticated())
                // Allow frames for the H2 console UI (for non-ignored endpoints, if necessary)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                // Use default form login and logout configuration
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable()); // for JWT later

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Secure password storage
    }
}
