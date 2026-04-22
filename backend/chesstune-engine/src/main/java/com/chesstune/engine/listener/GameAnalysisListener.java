package com.chesstune.engine.listener;

import com.chesstune.engine.config.RabbitConfig;
import com.chesstune.engine.dto.GameCompletedEvent;
import com.chesstune.engine.service.StockfishService;
import com.chesstune.engine.service.RPGStatsUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer — processes completed games via Stockfish analysis.
 *
 * Pipeline:
 * 1. Receive GameCompletedEvent from queue
 * 2. Parse PGN into individual positions
 * 3. Run Stockfish eval on each position
 * 4. Detect blunders (centipawn loss > threshold)
 * 5. Create UpsolveTask records for blunders
 * 6. Update RPGStats based on analysis
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GameAnalysisListener {

    private final StockfishService stockfishService;
    private final RPGStatsUpdater rpgStatsUpdater;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void analyzeGame(GameCompletedEvent event) {
        log.info("Received game for analysis: gameId={}, white={}, black={}",
                event.getGameId(), event.getWhiteId(), event.getBlackId());

        try {
            String pgn = event.getPgn();
            if (pgn == null || pgn.isBlank()) {
                log.warn("Empty PGN for game {}, skipping analysis", event.getGameId());
                return;
            }

            // In a full implementation:
            // 1. Parse PGN into a list of FEN positions
            // 2. For each position pair (before move, after move):
            //    - Get Stockfish eval before: bestScore
            //    - Get Stockfish eval after: actualScore
            //    - cpLoss = bestScore - actualScore
            //    - If isBlunder(cpLoss): create UpsolveTask
            // 3. Aggregate stats:
            //    - Opening accuracy: avg cp loss in moves 1-15
            //    - Tactical vision: % of tactical positions found
            //    - Endgame conversion: win rate from winning endgames
            //    - Time management: would need clock data

            log.info("Analysis complete for game {}. " +
                            "In production, this would create UpsolveTask records and update RPGStats.",
                    event.getGameId());

            // Example blunder detection pseudocode:
            // positions.forEach(pos -> {
            //     int bestEval = stockfishService.evaluate(pos.fenBefore());
            //     int afterEval = stockfishService.evaluate(pos.fenAfter());
            //     int cpLoss = Math.abs(bestEval - afterEval);
            //     if (stockfishService.isBlunder(cpLoss)) {
            //         String correctMove = stockfishService.bestMove(pos.fenBefore());
            //         // Save UpsolveTask to DB via JPA
            //     }
            // });

        } catch (Exception e) {
            log.error("Failed to analyze game {}: {}", event.getGameId(), e.getMessage(), e);
        }
    }
}
