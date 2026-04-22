package com.chesstune.core.repository;

import com.chesstune.core.entity.TournamentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, Long> {

    List<TournamentParticipant> findByTournamentIdOrderByCurrentScoreDescTiebreakScoreDesc(Long tournamentId);

    Optional<TournamentParticipant> findByTournamentIdAndUserId(Long tournamentId, Long userId);

    boolean existsByTournamentIdAndUserId(Long tournamentId, Long userId);

    long countByTournamentId(Long tournamentId);
}
