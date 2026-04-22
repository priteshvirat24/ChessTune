package com.chesstune.core.repository;

import com.chesstune.core.entity.TournamentPairing;
import com.chesstune.core.enums.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TournamentPairingRepository extends JpaRepository<TournamentPairing, Long> {

    List<TournamentPairing> findByRoundId(Long roundId);

    /** Get all opponent pairs across all rounds of a tournament to prevent rematches */
    @Query("SELECT p FROM TournamentPairing p WHERE p.round.tournament.id = :tournamentId")
    List<TournamentPairing> findAllByTournamentId(Long tournamentId);

    long countByRoundIdAndResult(Long roundId, GameResult result);
}
