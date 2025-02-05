package com.gameshelf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Completely ignore security filters for /h2-console/** endpoints
        return (web) -> web.ignoring().requestMatchers(new AntPathRequestMatcher("/h2-console/**"));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // For all other endpoints, let CSRF protection be enabled by default
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            // Allow frames for the H2 console UI (for non-ignored endpoints, if necessary)
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
            // Use default form login and logout configuration
            .formLogin(form -> form.permitAll())
            .logout(logout -> logout.permitAll());

        return http.build();
    }
}
