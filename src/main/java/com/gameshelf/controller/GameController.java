package com.gameshelf.controller;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Validated
public class GameController {
    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameRepository gameRepository;

    @PostMapping
    public ResponseEntity<Game> addGame(@Valid @RequestBody Game game, @AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
            
            log.debug("Adding game: {} for user: {}", game.getTitle(), user.getUsername());
            
            if (game.getTitle() == null || game.getGenre() == null || game.getPlatform() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required game fields missing");
            }
            
            game.setUser(user);
            Game savedGame = gameRepository.save(game);
            log.info("Successfully added game: {} for user: {}", savedGame.getTitle(), user.getUsername());
            return ResponseEntity.ok(savedGame);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding game: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding game");
        }
    }

    @GetMapping
    public ResponseEntity<Set<Game>> getGames(@AuthenticationPrincipal User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        log.debug("Retrieving games for user: {}", user.getUsername());
        return ResponseEntity.ok(user.getGames());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Game> updateGame(@PathVariable Long id, @Valid @RequestBody Game updatedGame, 
            @AuthenticationPrincipal User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        
        log.debug("Updating game with id: {} for user: {}", id, user.getUsername());
        
        return gameRepository.findById(id)
            .filter(game -> game.getUser() != null && game.getUser().getId().equals(user.getId()))
            .map(game -> {
                game.setTitle(updatedGame.getTitle());
                game.setGenre(updatedGame.getGenre());
                game.setPlatform(updatedGame.getPlatform());
                game.setRating(updatedGame.getRating());
                game.setReleaseDate(updatedGame.getReleaseDate());
                game.setNotes(updatedGame.getNotes());
                Game saved = gameRepository.save(game);
                log.info("Successfully updated game with id: {} for user: {}", id, user.getUsername());
                return ResponseEntity.ok(saved);
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found or unauthorized"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        
        log.debug("Deleting game with id: {} for user: {}", id, user.getUsername());
        
        return gameRepository.findById(id)
            .filter(game -> game.getUser() != null && game.getUser().getId().equals(user.getId()))
            .map(game -> {
                gameRepository.delete(game);
                log.info("Successfully deleted game with id: {} for user: {}", id, user.getUsername());
                return ResponseEntity.noContent().<Void>build();
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found or unauthorized"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Game> getGame(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        
        log.debug("Retrieving game with id: {} for user: {}", id, user.getUsername());
        
        return gameRepository.findById(id)
            .filter(game -> game.getUser() != null && game.getUser().getId().equals(user.getId()))
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found or unauthorized"));
    }
}
