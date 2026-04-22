package com.chesstune.core.repository;

import com.chesstune.core.entity.TournamentRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRoundRepository extends JpaRepository<TournamentRound, Long> {

    List<TournamentRound> findByTournamentIdOrderByRoundNumberAsc(Long tournamentId);

    Optional<TournamentRound> findByTournamentIdAndRoundNumber(Long tournamentId, Integer roundNumber);
}
