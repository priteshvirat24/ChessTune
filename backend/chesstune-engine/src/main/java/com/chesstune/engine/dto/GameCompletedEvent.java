package com.chesstune.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Event published by chesstune-core when a game finishes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameCompletedEvent implements Serializable {

    private Long gameId;
    private Long whiteId;
    private Long blackId;
    private String pgn;
    private String result;
    private Instant completedAt;
}
