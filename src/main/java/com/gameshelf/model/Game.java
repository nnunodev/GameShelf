package com.gameshelf.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entity representing a video game in the system.
 * This class stores information about individual games
 * and their relationship to users who own them.
 */
@Entity
@Table(name = "games")
@Data
public class Game {

    /**
     * Unique identifier for the game.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this game.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Title of the game.
     * Cannot be null or empty.
     */
    @Column(nullable = false)
    private String title;

    /**
     * Genre or category of the game.
     */
    @Column(nullable = false)
    private String genre;

    /**
     * Gaming platform the game is for (e.g., PS5, Xbox, PC).
     */
    @Column(nullable = false)
    private String platform;

    /**
     * User rating for the game (typically 1-5 stars).
     */
    private Double rating;

    /**
     * Release date of the game.
     */
    @Column(name = "release_date")
    private LocalDate releaseDate;

    /**
     * Personal notes or comments about the game.
     */
    @Column(length = 1000)
    private String notes;
}
