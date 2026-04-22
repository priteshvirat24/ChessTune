package com.chesstune.arena.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory game room state — managed by GameRoomService.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRoom {

    private String roomId;
    private Long whiteId;
    private Long blackId;
    private String whiteUsername;
    private String blackUsername;

    private String fen;
    @Builder.Default
    private List<String> moves = new ArrayList<>();

    private long whiteTimeMs;
    private long blackTimeMs;
    private String timeControl;

    @Builder.Default
    private GameRoomStatus status = GameRoomStatus.WAITING;

    private Long pairingId;   // if tournament game
    private Long tournamentId;

    public enum GameRoomStatus {
        WAITING, ACTIVE, COMPLETED
    }

    public boolean isWhiteTurn() {
        return moves.size() % 2 == 0;
    }
}
