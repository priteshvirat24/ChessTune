package com.chesstune.core.service;

import com.chesstune.core.entity.*;
import com.chesstune.core.enums.GameResult;
import com.chesstune.core.repository.TournamentPairingRepository;
import com.chesstune.core.repository.TournamentParticipantRepository;
import com.chesstune.core.repository.TournamentRoundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Swiss-system pairing algorithm (simplified Dutch system).
 *
 * 1. Sort participants by score (desc), then by rating (desc) for tiebreak
 * 2. Split sorted list into upper half and lower half
 * 3. Pair upper[i] with lower[i]
 * 4. Ensure no repeat pairings from previous rounds
 * 5. Alternate colors based on previous assignment
 * 6. Odd player count gets a "bye" (1 point awarded)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SwissPairingService {

    private final TournamentParticipantRepository participantRepository;
    private final TournamentRoundRepository roundRepository;
    private final TournamentPairingRepository pairingRepository;

    @Transactional
    public TournamentRound generatePairings(Tournament tournament, int roundNumber) {
        log.info("Generating Swiss pairings for tournament {} round {}",
                tournament.getId(), roundNumber);

        // Get all participants sorted by score, then rating
        List<TournamentParticipant> participants =
                participantRepository.findByTournamentIdOrderByCurrentScoreDescTiebreakScoreDesc(
                        tournament.getId());

        // Sort further by rating for equal scores
        participants.sort((a, b) -> {
            int scoreCompare = Double.compare(b.getCurrentScore(), a.getCurrentScore());
            if (scoreCompare != 0) return scoreCompare;
            return Integer.compare(b.getUser().getContestRating(), a.getUser().getContestRating());
        });

        // Collect previous pairings to avoid rematches
        Set<String> previousPairs = getPreviousPairKeys(tournament.getId());

        // Create the round
        TournamentRound round = TournamentRound.builder()
                .tournament(tournament)
                .roundNumber(roundNumber)
                .startTime(LocalDateTime.now())
                .build();
        round = roundRepository.save(round);

        // Handle bye for odd number of players
        TournamentParticipant byePlayer = null;
        List<TournamentParticipant> toPair = new ArrayList<>(participants);

        if (toPair.size() % 2 != 0) {
            // Give bye to lowest-ranked player who hasn't had a bye yet
            for (int i = toPair.size() - 1; i >= 0; i--) {
                // In a full implementation, track who has had byes
                byePlayer = toPair.remove(i);
                break;
            }
        }

        // Split into upper and lower halves
        int half = toPair.size() / 2;
        List<TournamentParticipant> upper = toPair.subList(0, half);
        List<TournamentParticipant> lower = toPair.subList(half, toPair.size());

        // Generate pairings with rematch avoidance
        List<TournamentPairing> pairings = new ArrayList<>();
        boolean[] lowerUsed = new boolean[lower.size()];

        for (int i = 0; i < upper.size(); i++) {
            TournamentParticipant white = upper.get(i);
            TournamentParticipant black = null;

            // Try to find a valid opponent (no rematch)
            for (int j = i; j < lower.size(); j++) {
                if (lowerUsed[j]) continue;

                String pairKey = makePairKey(white.getUser().getId(), lower.get(j).getUser().getId());
                if (!previousPairs.contains(pairKey)) {
                    black = lower.get(j);
                    lowerUsed[j] = true;
                    break;
                }
            }

            // Fallback: accept rematch if no valid opponent found
            if (black == null) {
                for (int j = 0; j < lower.size(); j++) {
                    if (!lowerUsed[j]) {
                        black = lower.get(j);
                        lowerUsed[j] = true;
                        break;
                    }
                }
            }

            if (black == null) continue;

            // Determine colors based on last assignment (alternate)
            User whiteUser = white.getUser();
            User blackUser = black.getUser();

            if ("WHITE".equals(white.getLastColor())) {
                // Swap colors
                User temp = whiteUser;
                whiteUser = blackUser;
                blackUser = temp;
                white.setLastColor("BLACK");
                black.setLastColor("WHITE");
            } else {
                white.setLastColor("WHITE");
                black.setLastColor("BLACK");
            }

            TournamentPairing pairing = TournamentPairing.builder()
                    .round(round)
                    .whiteUser(whiteUser)
                    .blackUser(blackUser)
                    .build();
            pairings.add(pairing);
        }

        // Save pairings
        pairingRepository.saveAll(pairings);

        // Award bye (1 point)
        if (byePlayer != null) {
            byePlayer.setCurrentScore(byePlayer.getCurrentScore() + 1.0);
            participantRepository.save(byePlayer);
            log.info("Bye awarded to player {}", byePlayer.getUser().getUsername());
        }

        // Save color assignments
        participantRepository.saveAll(toPair);

        log.info("Generated {} pairings for round {}", pairings.size(), roundNumber);
        return round;
    }

    /**
     * Calculate Buchholz tiebreak scores for all participants.
     * Buchholz = sum of opponents' scores.
     */
    @Transactional
    public void calculateTiebreaks(Long tournamentId) {
        List<TournamentParticipant> participants =
                participantRepository.findByTournamentIdOrderByCurrentScoreDescTiebreakScoreDesc(tournamentId);

        Map<Long, Double> userScores = participants.stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), TournamentParticipant::getCurrentScore));

        List<TournamentPairing> allPairings = pairingRepository.findAllByTournamentId(tournamentId);

        // Build opponent map
        Map<Long, List<Long>> opponentMap = new HashMap<>();
        for (TournamentPairing p : allPairings) {
            Long whiteId = p.getWhiteUser().getId();
            Long blackId = p.getBlackUser() != null ? p.getBlackUser().getId() : null;

            opponentMap.computeIfAbsent(whiteId, k -> new ArrayList<>());
            if (blackId != null) {
                opponentMap.get(whiteId).add(blackId);
                opponentMap.computeIfAbsent(blackId, k -> new ArrayList<>()).add(whiteId);
            }
        }

        // Calculate Buchholz
        for (TournamentParticipant p : participants) {
            Long userId = p.getUser().getId();
            List<Long> opponents = opponentMap.getOrDefault(userId, List.of());
            double buchholz = opponents.stream()
                    .mapToDouble(oppId -> userScores.getOrDefault(oppId, 0.0))
                    .sum();
            p.setTiebreakScore(buchholz);
        }

        participantRepository.saveAll(participants);
    }

    /**
     * Update rankings after tiebreaks are calculated.
     */
    @Transactional
    public void updateRankings(Long tournamentId) {
        calculateTiebreaks(tournamentId);

        List<TournamentParticipant> participants =
                participantRepository.findByTournamentIdOrderByCurrentScoreDescTiebreakScoreDesc(tournamentId);

        for (int i = 0; i < participants.size(); i++) {
            participants.get(i).setRank(i + 1);
        }

        participantRepository.saveAll(participants);
    }

    private Set<String> getPreviousPairKeys(Long tournamentId) {
        return pairingRepository.findAllByTournamentId(tournamentId).stream()
                .filter(p -> p.getBlackUser() != null)
                .map(p -> makePairKey(p.getWhiteUser().getId(), p.getBlackUser().getId()))
                .collect(Collectors.toSet());
    }

    private String makePairKey(Long id1, Long id2) {
        long min = Math.min(id1, id2);
        long max = Math.max(id1, id2);
        return min + "-" + max;
    }
}
