package com.chesstune.core.service;

import com.chesstune.core.dto.PairingDTO;
import com.chesstune.core.dto.TournamentDTO;
import com.chesstune.core.entity.*;
import com.chesstune.core.enums.TournamentStatus;
import com.chesstune.core.exception.ResourceNotFoundException;
import com.chesstune.core.exception.UpsolvePendingException;
import com.chesstune.core.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository participantRepository;
    private final TournamentRoundRepository roundRepository;
    private final TournamentPairingRepository pairingRepository;
    private final UserRepository userRepository;
    private final UpsolveTaskRepository upsolveTaskRepository;
    private final SwissPairingService swissPairingService;
    private final RatingService ratingService;

    public List<TournamentDTO> getUpcomingAndActiveTournaments() {
        return tournamentRepository.findByStatusInOrderByStartTimeAsc(
                List.of(TournamentStatus.UPCOMING, TournamentStatus.ACTIVE))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TournamentDTO getTournamentDetails(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + id));

        TournamentDTO dto = toDTO(tournament);

        // Include standings
        List<TournamentParticipant> standings =
                participantRepository.findByTournamentIdOrderByCurrentScoreDescTiebreakScoreDesc(id);
        dto.setStandings(standings.stream().map(p -> TournamentDTO.ParticipantDTO.builder()
                .userId(p.getUser().getId())
                .username(p.getUser().getUsername())
                .score(p.getCurrentScore())
                .tiebreakScore(p.getTiebreakScore())
                .rank(p.getRank())
                .division(p.getUser().getCurrentDivision().name())
                .rating(p.getUser().getContestRating())
                .build()).collect(Collectors.toList()));

        return dto;
    }

    /**
     * Register a user for a tournament.
     * Enforces division restriction and upsolve gate.
     */
    @Transactional
    public void registerUser(Long tournamentId, Long userId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        if (tournament.getStatus() != TournamentStatus.UPCOMING) {
            throw new IllegalArgumentException("Can only register for upcoming tournaments");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Division gate
        if (tournament.getDivisionAllowed() != null &&
                tournament.getDivisionAllowed() != user.getCurrentDivision()) {
            throw new IllegalArgumentException(
                    "This tournament is restricted to " + tournament.getDivisionAllowed());
        }

        // Upsolve gate — block if user has pending tasks
        long pendingTasks = upsolveTaskRepository.countByUserIdAndIsSolvedFalse(userId);
        if (pendingTasks > 0) {
            throw new UpsolvePendingException(
                    "Complete " + pendingTasks + " upsolve tasks before registering for tournaments");
        }

        // Duplicate check
        if (participantRepository.existsByTournamentIdAndUserId(tournamentId, userId)) {
            throw new IllegalArgumentException("Already registered for this tournament");
        }

        TournamentParticipant participant = TournamentParticipant.builder()
                .tournament(tournament)
                .user(user)
                .build();
        participantRepository.save(participant);

        log.info("User {} registered for tournament {}", user.getUsername(), tournament.getTitle());
    }

    /**
     * Start the tournament and generate Round 1 pairings.
     */
    @Transactional
    public TournamentRound startTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        if (tournament.getStatus() != TournamentStatus.UPCOMING) {
            throw new IllegalArgumentException("Tournament is not in UPCOMING state");
        }

        long count = participantRepository.countByTournamentId(tournamentId);
        if (count < 2) {
            throw new IllegalArgumentException("Need at least 2 participants to start");
        }

        tournament.setStatus(TournamentStatus.ACTIVE);
        tournament.setCurrentRound(1);
        tournamentRepository.save(tournament);

        return swissPairingService.generatePairings(tournament, 1);
    }

    /**
     * Advance to the next round (called after all games in current round finish).
     */
    @Transactional
    public TournamentRound advanceRound(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        if (tournament.getStatus() != TournamentStatus.ACTIVE) {
            throw new IllegalArgumentException("Tournament is not ACTIVE");
        }

        int nextRound = tournament.getCurrentRound() + 1;

        if (nextRound > tournament.getTotalRounds()) {
            // Tournament is over
            return completeTournament(tournamentId);
        }

        tournament.setCurrentRound(nextRound);
        tournamentRepository.save(tournament);

        return swissPairingService.generatePairings(tournament, nextRound);
    }

    /**
     * Complete tournament — calculate final standings, rating changes, and promotions.
     */
    @Transactional
    public TournamentRound completeTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        tournament.setStatus(TournamentStatus.COMPLETED);
        tournamentRepository.save(tournament);

        // Calculate final tiebreaks and rankings
        swissPairingService.updateRankings(tournamentId);

        // Get final standings
        List<TournamentParticipant> standings =
                participantRepository.findByTournamentIdOrderByCurrentScoreDescTiebreakScoreDesc(tournamentId);

        // Process rating changes
        ratingService.processRatingChanges(standings);

        // Process division promotions/demotions
        ratingService.processPromotions(standings);

        log.info("Tournament {} completed with {} participants", tournament.getTitle(), standings.size());
        return null;
    }

    public List<PairingDTO> getRoundPairings(Long tournamentId, Integer roundNumber) {
        TournamentRound round = roundRepository.findByTournamentIdAndRoundNumber(tournamentId, roundNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));

        return pairingRepository.findByRoundId(round.getId()).stream()
                .map(p -> PairingDTO.builder()
                        .id(p.getId())
                        .roundNumber(roundNumber)
                        .whiteUserId(p.getWhiteUser().getId())
                        .whiteUsername(p.getWhiteUser().getUsername())
                        .blackUserId(p.getBlackUser() != null ? p.getBlackUser().getId() : null)
                        .blackUsername(p.getBlackUser() != null ? p.getBlackUser().getUsername() : "BYE")
                        .result(p.getResult().name())
                        .gameId(p.getGameId())
                        .build())
                .collect(Collectors.toList());
    }

    /** Admin: Create a new tournament */
    @Transactional
    public Tournament createTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    /** Admin: Kick a player from a tournament */
    @Transactional
    public void kickPlayer(Long tournamentId, Long userId) {
        TournamentParticipant participant = participantRepository
                .findByTournamentIdAndUserId(tournamentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));
        participantRepository.delete(participant);
        log.info("Player {} kicked from tournament {}", userId, tournamentId);
    }

    private TournamentDTO toDTO(Tournament t) {
        return TournamentDTO.builder()
                .id(t.getId())
                .title(t.getTitle())
                .startTime(t.getStartTime())
                .status(t.getStatus().name())
                .divisionAllowed(t.getDivisionAllowed() != null ? t.getDivisionAllowed().name() : null)
                .totalRounds(t.getTotalRounds())
                .currentRound(t.getCurrentRound())
                .timeControl(t.getTimeControl())
                .participantCount(participantRepository.countByTournamentId(t.getId()))
                .build();
    }
}
