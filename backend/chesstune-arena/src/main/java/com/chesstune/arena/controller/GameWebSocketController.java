package com.chesstune.arena.controller;

import com.chesstune.arena.model.GameRoom;
import com.chesstune.arena.service.ClockService;
import com.chesstune.arena.service.GameRoomService;
import com.chesstune.arena.service.LeaderboardService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket controller handling game moves, resignations, and draw offers.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class GameWebSocketController {

    private final GameRoomService gameRoomService;
    private final ClockService clockService;
    private final LeaderboardService leaderboardService;

    /**
     * Handle a player's move.
     * Client sends to: /app/game/{roomId}/move
     */
    @MessageMapping("/game/{roomId}/move")
    public void handleMove(
            @DestinationVariable String roomId,
            @Payload MoveMessage move,
            Principal principal) {

        GameRoom room = gameRoomService.getRoom(roomId);
        if (room == null || room.getStatus() != GameRoom.GameRoomStatus.ACTIVE) {
            log.warn("Move rejected: room {} not active", roomId);
            return;
        }

        // Validate it's the player's turn
        String username = principal.getName();
        boolean isWhiteTurn = room.isWhiteTurn();

        if ((isWhiteTurn && !username.equals(room.getWhiteUsername())) ||
                (!isWhiteTurn && !username.equals(room.getBlackUsername()))) {
            log.warn("Move rejected: not {}'s turn in room {}", username, roomId);
            return;
        }

        // Record the move (FEN update would be done by chess.js on client,
        // server trusts client's FEN for now — in production, validate server-side)
        room.getMoves().add(move.getFrom() + move.getTo() +
                (move.getPromotion() != null ? move.getPromotion() : ""));

        if (move.getFen() != null) {
            room.setFen(move.getFen());
        }

        // Add increment after the move
        clockService.addIncrement(roomId, isWhiteTurn);

        // Update and broadcast
        gameRoomService.updateRoom(room);
        gameRoomService.broadcastGameState(roomId);

        log.debug("Move in room {}: {} played {}{}", roomId, username, move.getFrom(), move.getTo());
    }

    /**
     * Handle resignation.
     * Client sends to: /app/game/{roomId}/resign
     */
    @MessageMapping("/game/{roomId}/resign")
    public void handleResign(
            @DestinationVariable String roomId,
            Principal principal) {

        GameRoom room = gameRoomService.getRoom(roomId);
        if (room == null) return;

        String username = principal.getName();
        String result = username.equals(room.getWhiteUsername()) ? "BLACK_WIN" : "WHITE_WIN";

        room.setStatus(GameRoom.GameRoomStatus.COMPLETED);
        gameRoomService.updateRoom(room);
        clockService.stopClock(roomId);
        gameRoomService.broadcastGameEnd(roomId, result);

        log.info("{} resigned in room {}", username, roomId);
    }

    /**
     * Handle game over (checkmate, stalemate, etc. detected by client).
     * Client sends to: /app/game/{roomId}/gameover
     */
    @MessageMapping("/game/{roomId}/gameover")
    public void handleGameOver(
            @DestinationVariable String roomId,
            @Payload GameOverMessage message) {

        GameRoom room = gameRoomService.getRoom(roomId);
        if (room == null) return;

        room.setStatus(GameRoom.GameRoomStatus.COMPLETED);
        gameRoomService.updateRoom(room);
        clockService.stopClock(roomId);
        gameRoomService.broadcastGameEnd(roomId, message.getResult());

        // Update tournament leaderboard if applicable
        if (room.getTournamentId() != null) {
            // Score updates would be pushed here
            leaderboardService.broadcastLeaderboard(room.getTournamentId());
        }

        log.info("Game over in room {}: {}", roomId, message.getResult());
    }

    @Data
    public static class MoveMessage {
        private String from;
        private String to;
        private String promotion;
        private String fen;
    }

    @Data
    public static class GameOverMessage {
        private String result; // WHITE_WIN, BLACK_WIN, DRAW
    }
}
