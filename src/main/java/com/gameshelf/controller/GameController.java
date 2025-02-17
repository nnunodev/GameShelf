package com.gameshelf.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gameshelf.model.Game;
import com.gameshelf.model.User;
import com.gameshelf.repository.GameRepository;
import com.gameshelf.repository.UserRepository;



@RestController
public class GameController {
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/games")
    public Game addGame(@RequestBody Game game, @AuthenticationPrincipal User user) {
        Game savedGame = gameRepository.save(game);
        user.getGames().add(savedGame);
        userRepository.save(user);
        return savedGame;
    }

    @GetMapping("/games")
    public Set<Game> getGames(@AuthenticationPrincipal User user) {
        return user.getGames();
    }
    
}
