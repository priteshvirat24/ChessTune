package com.chesstune.arena.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Redis-backed live leaderboard using sorted sets.
 * Updates instantly when a game finishes, broadcasts top 100 to spectators.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LeaderboardService {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String KEY_PREFIX = "chesstune:tournament:";

    /**
     * Update a player's score in the tournament leaderboard.
     */
    public void updateScore(Long tournamentId, Long userId, String username, double score) {
        String key = KEY_PREFIX + tournamentId + ":leaderboard";
        redisTemplate.opsForZSet().add(key, userId + ":" + username, score);
        log.debug("Leaderboard updated: tournament={}, user={}, score={}",
                tournamentId, username, score);
    }

    /**
     * Get top 100 entries from the leaderboard.
     */
    public List<Map<String, Object>> getTop100(Long tournamentId) {
        String key = KEY_PREFIX + tournamentId + ":leaderboard";
        Set<ZSetOperations.TypedTuple<String>> entries =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 99);

        if (entries == null) return List.of();

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> entry : entries) {
            String[] parts = entry.getValue().split(":", 2);
            result.add(Map.of(
                    "rank", rank++,
                    "userId", Long.parseLong(parts[0]),
                    "username", parts.length > 1 ? parts[1] : "unknown",
                    "score", entry.getScore()
            ));
        }
        return result;
    }

    /**
     * Broadcast the live leaderboard to all spectators.
     */
    public void broadcastLeaderboard(Long tournamentId) {
        List<Map<String, Object>> top100 = getTop100(tournamentId);
        messagingTemplate.convertAndSend(
                "/topic/tournament/" + tournamentId + "/leaderboard", top100);
    }

    /**
     * Clean up leaderboard when tournament completes.
     */
    public void clearLeaderboard(Long tournamentId) {
        String key = KEY_PREFIX + tournamentId + ":leaderboard";
        redisTemplate.delete(key);
    }
}
