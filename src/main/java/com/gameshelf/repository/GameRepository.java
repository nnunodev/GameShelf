package com.gameshelf.repository;
import com.gameshelf.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository <Game,Long>{
    List <Game> findByGenre(String genre);
    List <Game> findByPlatform(String platform);
    List <Game> findByName(String name);


}
