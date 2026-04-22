package com.chesstune.core.repository;

import com.chesstune.core.entity.RPGStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RPGStatsRepository extends JpaRepository<RPGStats, Long> {

    Optional<RPGStats> findByUserId(Long userId);
}
