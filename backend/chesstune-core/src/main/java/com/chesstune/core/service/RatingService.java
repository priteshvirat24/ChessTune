package com.chesstune.core.service;

import com.chesstune.core.entity.TournamentParticipant;
import com.chesstune.core.entity.User;
import com.chesstune.core.enums.Division;
import com.chesstune.core.repository.TournamentParticipantRepository;
import com.chesstune.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Elo-like rating calculation inspired by Codeforces rating deltas.
 * Also handles division promotions/demotions after tournaments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final UserRepository userRepository;

    private static final int K_FACTOR = 32;

    /**
     * Calculate and apply rating changes after a tournament completes.
     * Uses expected vs actual performance relative to opponents.
     */
    @Transactional
    public void processRatingChanges(List<TournamentParticipant> standings) {
        int n = standings.size();
        if (n < 2) return;

        for (TournamentParticipant participant : standings) {
            User user = participant.getUser();
            int currentRating = user.getContestRating();

            // Calculate expected performance based on rank
            double expectedRank = calculateExpectedRank(currentRating, standings);
            double actualRank = participant.getRank();

            // Codeforces-style delta: better rank than expected = positive delta
            int delta = (int) Math.round(K_FACTOR * (expectedRank - actualRank) / n);

            // Clamp delta
            delta = Math.max(-150, Math.min(150, delta));

            int newRating = Math.max(100, currentRating + delta);
            user.setContestRating(newRating);

            log.info("Rating change for {}: {} -> {} (delta: {})",
                    user.getUsername(), currentRating, newRating, delta);
        }

        // Save all updated users
        List<User> users = standings.stream()
                .map(TournamentParticipant::getUser)
                .toList();
        userRepository.saveAll(users);
    }

    /**
     * Promote/demote users between divisions based on their rank in the tournament.
     * Top 25% → promote, Bottom 25% → demote.
     */
    @Transactional
    public void processPromotions(List<TournamentParticipant> standings) {
        int n = standings.size();
        if (n < 4) return; // Need at least 4 players for meaningful promotion

        int promoteThreshold = n / 4;
        int demoteThreshold = n - (n / 4);

        for (TournamentParticipant p : standings) {
            User user = p.getUser();
            Division current = user.getCurrentDivision();
            int rank = p.getRank();

            if (rank <= promoteThreshold) {
                // Promote
                Division promoted = switch (current) {
                    case DIV_3 -> Division.DIV_2;
                    case DIV_2 -> Division.DIV_1;
                    case DIV_1 -> Division.DIV_1; // Already top
                };
                if (promoted != current) {
                    user.setCurrentDivision(promoted);
                    log.info("PROMOTED: {} from {} to {}", user.getUsername(), current, promoted);
                }
            } else if (rank > demoteThreshold) {
                // Demote
                Division demoted = switch (current) {
                    case DIV_1 -> Division.DIV_2;
                    case DIV_2 -> Division.DIV_3;
                    case DIV_3 -> Division.DIV_3; // Already bottom
                };
                if (demoted != current) {
                    user.setCurrentDivision(demoted);
                    log.info("DEMOTED: {} from {} to {}", user.getUsername(), current, demoted);
                }
            }
        }

        List<User> users = standings.stream()
                .map(TournamentParticipant::getUser)
                .toList();
        userRepository.saveAll(users);
    }

    private double calculateExpectedRank(int rating, List<TournamentParticipant> standings) {
        double expected = 1.0;
        for (TournamentParticipant other : standings) {
            if (other.getUser().getContestRating() != rating) {
                double diff = other.getUser().getContestRating() - rating;
                double winProb = 1.0 / (1.0 + Math.pow(10, diff / 400.0));
                expected += (1.0 - winProb);
            }
        }
        return expected;
    }
}
