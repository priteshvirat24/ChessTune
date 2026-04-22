package com.chesstune.core.service;

import com.chesstune.core.dto.UserProfileResponse;
import com.chesstune.core.entity.RPGStats;
import com.chesstune.core.entity.User;
import com.chesstune.core.repository.RPGStatsRepository;
import com.chesstune.core.repository.UpsolveTaskRepository;
import com.chesstune.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User profile, leaderboard, and upsolve task queries.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RPGStatsRepository rpgStatsRepository;
    private final UpsolveTaskRepository upsolveTaskRepository;

    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        RPGStats stats = rpgStatsRepository.findByUserId(userId)
                .orElse(RPGStats.builder().build());

        long pendingTasks = upsolveTaskRepository.countByUserIdAndIsSolvedFalse(userId);

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .division(user.getCurrentDivision().name())
                .contestRating(user.getContestRating())
                .openingAccuracy(stats.getOpeningAccuracy())
                .tacticalVision(stats.getTacticalVision())
                .endgameConversion(stats.getEndgameConversion())
                .timeManagement(stats.getTimeManagement())
                .pendingUpsolveTasks(pendingTasks)
                .build();
    }

    public UserProfileResponse getUserProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return getUserProfile(user.getId());
    }

    public List<UserProfileResponse> getLeaderboard() {
        return userRepository.findTop100ByOrderByContestRatingDesc()
                .stream()
                .map(user -> UserProfileResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .division(user.getCurrentDivision().name())
                        .contestRating(user.getContestRating())
                        .build())
                .collect(Collectors.toList());
    }
}
