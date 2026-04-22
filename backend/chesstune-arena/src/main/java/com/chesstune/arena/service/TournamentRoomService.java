package com.chesstune.arena.service;

import com.chesstune.arena.model.GameRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Manages tournament-specific game room creation.
 * When pairings are generated, this service creates rooms and notifies players.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TournamentRoomService {

    private final GameRoomService gameRoomService;
    private final ClockService clockService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Create a game room for a tournament pairing and notify both players.
     */
    public GameRoom createTournamentGame(
            Long tournamentId, Long pairingId,
            Long whiteId, String whiteUsername,
            Long blackId, String blackUsername,
            String timeControl) {

        GameRoom room = gameRoomService.createRoom(
                whiteId, whiteUsername, blackId, blackUsername,
                timeControl, pairingId, tournamentId);

        // Start the clock
        clockService.startClock(room.getRoomId());

        // Notify players via user-specific queues
        Map<String, Object> notification = Map.of(
                "type", "GAME_START",
                "roomId", room.getRoomId(),
                "opponent", blackUsername,
                "color", "WHITE",
                "timeControl", timeControl,
                "tournamentId", tournamentId
        );
        messagingTemplate.convertAndSendToUser(
                whiteUsername, "/queue/notifications", notification);

        Map<String, Object> blackNotification = Map.of(
                "type", "GAME_START",
                "roomId", room.getRoomId(),
                "opponent", whiteUsername,
                "color", "BLACK",
                "timeControl", timeControl,
                "tournamentId", tournamentId
        );
        messagingTemplate.convertAndSendToUser(
                blackUsername, "/queue/notifications", blackNotification);

        log.info("Tournament game room {} created: {} vs {} (tournament {})",
                room.getRoomId(), whiteUsername, blackUsername, tournamentId);

        return room;
    }
}
