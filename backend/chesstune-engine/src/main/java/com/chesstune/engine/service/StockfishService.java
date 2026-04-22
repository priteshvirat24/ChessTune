package com.chesstune.engine.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * Stockfish UCI protocol integration.
 * Runs Stockfish as a subprocess and communicates via stdin/stdout.
 */
@Service
@Slf4j
public class StockfishService {

    private final String stockfishPath;
    private final int analysisDepth;
    private final int blunderThresholdCp;
    private final int mistakeThresholdCp;

    public StockfishService(
            @Value("${chesstune.stockfish.path:/usr/local/bin/stockfish}") String stockfishPath,
            @Value("${chesstune.stockfish.depth:20}") int analysisDepth,
            @Value("${chesstune.stockfish.blunder-threshold-cp:150}") int blunderThresholdCp,
            @Value("${chesstune.stockfish.mistake-threshold-cp:75}") int mistakeThresholdCp) {
        this.stockfishPath = stockfishPath;
        this.analysisDepth = analysisDepth;
        this.blunderThresholdCp = blunderThresholdCp;
        this.mistakeThresholdCp = mistakeThresholdCp;
    }

    /**
     * Evaluate a position and return the score in centipawns.
     * Positive = white advantage, Negative = black advantage.
     */
    public int evaluate(String fen) {
        try {
            Process process = startStockfish();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream()));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            // Set position
            writer.write("position fen " + fen + "\n");
            writer.write("go depth " + analysisDepth + "\n");
            writer.flush();

            int score = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("bestmove")) break;
                if (line.contains("score cp")) {
                    String[] parts = line.split("score cp ");
                    if (parts.length > 1) {
                        score = Integer.parseInt(parts[1].split(" ")[0]);
                    }
                } else if (line.contains("score mate")) {
                    String[] parts = line.split("score mate ");
                    if (parts.length > 1) {
                        int mate = Integer.parseInt(parts[1].split(" ")[0]);
                        score = mate > 0 ? 10000 : -10000;
                    }
                }
            }

            process.destroy();
            return score;

        } catch (Exception e) {
            log.error("Stockfish evaluation failed for FEN {}: {}", fen, e.getMessage());
            return 0; // fallback
        }
    }

    /**
     * Get the best move for a position.
     */
    public String bestMove(String fen) {
        try {
            Process process = startStockfish();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream()));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            writer.write("position fen " + fen + "\n");
            writer.write("go depth " + analysisDepth + "\n");
            writer.flush();

            String bestMove = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    bestMove = line.split(" ")[1];
                    break;
                }
            }

            process.destroy();
            return bestMove;

        } catch (Exception e) {
            log.error("Stockfish bestmove failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if a centipawn loss qualifies as a blunder.
     */
    public boolean isBlunder(int cpLoss) {
        return Math.abs(cpLoss) >= blunderThresholdCp;
    }

    /**
     * Check if a centipawn loss qualifies as a mistake.
     */
    public boolean isMistake(int cpLoss) {
        return Math.abs(cpLoss) >= mistakeThresholdCp;
    }

    private Process startStockfish() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(stockfishPath);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream()));
        writer.write("uci\n");
        writer.write("isready\n");
        writer.flush();

        // Wait for readyok
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals("readyok")) break;
        }

        return process;
    }
}
