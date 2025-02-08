package com.gameshelf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.gameshelf.security.JwtUtil;

@SpringBootApplication
public class GameShelfApplication {

	public static void main(String[] args) {
        // Start the Spring Boot application **only once**
		ApplicationContext context = SpringApplication.run(GameShelfApplication.class, args);

		// Get the JWT utility bean from the application context
		JwtUtil jwtUtil = context.getBean(JwtUtil.class); 

		// Generate and print a test JWT token
		String token = jwtUtil.generateToken("testuser");

		System.out.println("Generated Token: " + token);
        System.out.println("Extracted Username: " + jwtUtil.extractUsername(token));
        System.out.println("Token Valid: " + jwtUtil.validateToken(token, "testuser"));
	}
}
