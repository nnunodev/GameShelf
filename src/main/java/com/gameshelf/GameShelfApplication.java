package com.gameshelf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GameShelfApplication {
		
		private static final Logger logger = LoggerFactory.getLogger(GameShelfApplication.class);

		public static void main(String[] args) {
				logger.info("Starting GameShelf Application...");
				SpringApplication.run(GameShelfApplication.class, args);
				logger.info("GameShelf Application is running");
		}
}