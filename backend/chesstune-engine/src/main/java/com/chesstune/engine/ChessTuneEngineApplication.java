package com.chesstune.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ChessTune Engine — Async Worker Entry Point.
 * Listens to RabbitMQ for completed games, runs Stockfish analysis,
 * generates UpsolveTasks, and updates RPG stats.
 */
@SpringBootApplication
public class ChessTuneEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChessTuneEngineApplication.class, args);
    }
}
