package com.chesstune.core.service;

import com.chesstune.core.config.RabbitConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.Instant;

/**
 * Publishes game completion events to RabbitMQ for async Stockfish analysis.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GameCompletedPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishGameCompleted(Long gameId, Long whiteId, Long blackId,
                                     String pgn, String result) {
        GameCompletedEvent event = new GameCompletedEvent(
                gameId, whiteId, blackId, pgn, result, Instant.now());

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                event);

        log.info("Published game.completed event for game {}", gameId);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameCompletedEvent implements Serializable {
        private Long gameId;
        private Long whiteId;
        private Long blackId;
        private String pgn;
        private String result;
        private Instant completedAt;
    }
}
