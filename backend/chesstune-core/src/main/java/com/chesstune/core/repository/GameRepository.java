package com.chesstune.core.repository;

import com.chesstune.core.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByWhiteIdOrBlackIdOrderByCreatedAtDesc(Long whiteId, Long blackId);
}
