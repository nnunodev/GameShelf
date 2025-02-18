package com.gameshelf.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordDebugUtil implements CommandLineRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String testPassword = "Testpassword123";
        String encoded = passwordEncoder.encode(testPassword);
        System.out.println("Debug - Test password encoding:");
        System.out.println("Raw password: " + testPassword);
        System.out.println("Encoded: " + encoded);
        System.out.println("Matches: " + passwordEncoder.matches(testPassword, encoded));
    }
}
