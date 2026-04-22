package com.chesstune.arena;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ChessTune Arena — WebSocket Service Entry Point.
 * Manages active game rooms, matchmaking, real-time clocks, and live leaderboards.
 */
@SpringBootApplication
public class ChessTuneArenaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChessTuneArenaApplication.class, args);
    }
}
