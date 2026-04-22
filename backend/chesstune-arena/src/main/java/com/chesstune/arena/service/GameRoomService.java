package com.chesstune.arena.service;

import com.chesstune.arena.model.GameRoom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active game rooms in memory.
 */
@Service
@Slf4j
public class GameRoomService {

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public GameRoomService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public GameRoom createRoom(Long whiteId, String whiteUsername,
                               Long blackId, String blackUsername,
                               String timeControl, Long pairingId, Long tournamentId) {
        String roomId = UUID.randomUUID().toString().substring(0, 8);

        // Parse time control (e.g., "5+3" → 300000ms + 3000ms increment)
        long baseTimeMs = 300_000; // default 5 min
        try {
            String[] parts = timeControl.split("\\+");
            baseTimeMs = Long.parseLong(parts[0]) * 60_000;
        } catch (Exception ignored) {}

        GameRoom room = GameRoom.builder()
                .roomId(roomId)
                .whiteId(whiteId)
                .blackId(blackId)
                .whiteUsername(whiteUsername)
                .blackUsername(blackUsername)
                .fen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
                .whiteTimeMs(baseTimeMs)
                .blackTimeMs(baseTimeMs)
                .timeControl(timeControl)
                .pairingId(pairingId)
                .tournamentId(tournamentId)
                .status(GameRoom.GameRoomStatus.ACTIVE)
                .build();

        rooms.put(roomId, room);
        log.info("Game room {} created: {} vs {}", roomId, whiteUsername, blackUsername);
        return room;
    }

    public GameRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public void updateRoom(GameRoom room) {
        rooms.put(room.getRoomId(), room);
    }

    public void removeRoom(String roomId) {
        rooms.remove(roomId);
    }

    /**
     * Broadcast game state to all subscribers of a room.
     */
    public void broadcastGameState(String roomId) {
        GameRoom room = rooms.get(roomId);
        if (room == null) return;

        messagingTemplate.convertAndSend("/topic/game/" + roomId, Map.of(
                "fen", room.getFen(),
                "moves", room.getMoves(),
                "whiteTimeMs", room.getWhiteTimeMs(),
                "blackTimeMs", room.getBlackTimeMs(),
                "status", room.getStatus().name(),
                "isWhiteTurn", room.isWhiteTurn()
        ));
    }

    /**
     * Broadcast game end notification.
     */
    public void broadcastGameEnd(String roomId, String result) {
        messagingTemplate.convertAndSend("/topic/game/" + roomId + "/end", Map.of(
                "result", result,
                "roomId", roomId
        ));
    }
}
