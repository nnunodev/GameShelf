package com.gameshelf.controller;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gameshelf.model.Game;
import com.gameshelf.model.User;
import com.gameshelf.repository.GameRepository;
import com.gameshelf.repository.UserRepository;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(@AuthenticationPrincipal User user) {
        if (user != null) {
            return ResponseEntity.ok("Authenticated as: " + user.getUsername());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
    }

    @PostMapping
    public ResponseEntity<Game> addGame(@RequestBody Game game) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            logger.debug("Adding game: {} for authenticated user: {}", game.getTitle(), username);
            
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            
            game.setUser(user);
            Game savedGame = gameRepository.save(game);
            return ResponseEntity.ok(savedGame);
        } catch (Exception e) {
            logger.error("Error adding game: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding game");
        }
    }

    @GetMapping
    public ResponseEntity<Set<Game>> getGames(@AuthenticationPrincipal User user) {
        if (user == null) {
            logger.error("Unauthorized attempt to get games");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(user.getGames());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Game> updateGame(@PathVariable Long id, @RequestBody Game updatedGame, @AuthenticationPrincipal User user) {
        Optional<Game> optionalGame = gameRepository.findById(id);
        if (optionalGame.isPresent()) {
            Game game = optionalGame.get();
            game.setTitle(updatedGame.getTitle());
            game.setGenre(updatedGame.getGenre());
            game.setPlatform(updatedGame.getPlatform());
            game.setRating(updatedGame.getRating());
            game.setReleaseDate(updatedGame.getReleaseDate());
            game.setNotes(updatedGame.getNotes());
            Game savedGame = gameRepository.save(game);
            return ResponseEntity.ok(savedGame);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGame(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Optional<Game> optionalGame = gameRepository.findById(id);
        if (optionalGame.isPresent()) {
            Game game = optionalGame.get();
            user.getGames().remove(game);
            userRepository.save(user);
            gameRepository.delete(game);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
