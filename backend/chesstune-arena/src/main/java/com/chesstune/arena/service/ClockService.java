package com.chesstune.arena.service;

import com.chesstune.arena.model.GameRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Manages chess clocks for active games.
 * Ticks every 100ms, auto-adjudicates on timeout.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClockService {

    private final GameRoomService gameRoomService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> clockTasks = new ConcurrentHashMap<>();

    public void startClock(String roomId) {
        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
            try {
                tick(roomId);
            } catch (Exception e) {
                log.error("Clock tick error for room {}: {}", roomId, e.getMessage());
            }
        }, 100, 100, TimeUnit.MILLISECONDS);

        clockTasks.put(roomId, task);
        log.info("Clock started for room {}", roomId);
    }

    public void stopClock(String roomId) {
        ScheduledFuture<?> task = clockTasks.remove(roomId);
        if (task != null) {
            task.cancel(false);
            log.info("Clock stopped for room {}", roomId);
        }
    }

    /**
     * Add increment after a move (e.g., +3 sec for 5+3 control).
     */
    public void addIncrement(String roomId, boolean isWhite) {
        GameRoom room = gameRoomService.getRoom(roomId);
        if (room == null) return;

        try {
            String[] parts = room.getTimeControl().split("\\+");
            if (parts.length > 1) {
                long incrementMs = Long.parseLong(parts[1]) * 1000;
                if (isWhite) {
                    room.setWhiteTimeMs(room.getWhiteTimeMs() + incrementMs);
                } else {
                    room.setBlackTimeMs(room.getBlackTimeMs() + incrementMs);
                }
                gameRoomService.updateRoom(room);
            }
        } catch (Exception ignored) {}
    }

    private void tick(String roomId) {
        GameRoom room = gameRoomService.getRoom(roomId);
        if (room == null || room.getStatus() != GameRoom.GameRoomStatus.ACTIVE) {
            stopClock(roomId);
            return;
        }

        // Decrement active player's clock
        if (room.isWhiteTurn()) {
            room.setWhiteTimeMs(room.getWhiteTimeMs() - 100);
            if (room.getWhiteTimeMs() <= 0) {
                room.setWhiteTimeMs(0);
                handleTimeout(roomId, "BLACK_WIN");
                return;
            }
        } else {
            room.setBlackTimeMs(room.getBlackTimeMs() - 100);
            if (room.getBlackTimeMs() <= 0) {
                room.setBlackTimeMs(0);
                handleTimeout(roomId, "WHITE_WIN");
                return;
            }
        }

        gameRoomService.updateRoom(room);
    }

    private void handleTimeout(String roomId, String result) {
        log.info("Timeout in room {}: {}", roomId, result);
        stopClock(roomId);

        GameRoom room = gameRoomService.getRoom(roomId);
        if (room != null) {
            room.setStatus(GameRoom.GameRoomStatus.COMPLETED);
            gameRoomService.updateRoom(room);
            gameRoomService.broadcastGameEnd(roomId, result);
        }
    }
}
