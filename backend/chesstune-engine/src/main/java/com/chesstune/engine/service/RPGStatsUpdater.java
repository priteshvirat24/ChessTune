package com.chesstune.engine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Updates RPG stats based on game analysis.
 * Uses rolling weighted averages (recent 20 games weighted more).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RPGStatsUpdater {

    private static final double WEIGHT = 0.1; // blend factor for new data

    /**
     * Calculate opening accuracy from centipawn losses in moves 1-15.
     * Returns 0-100 score (100 = perfect).
     */
    public double calculateOpeningAccuracy(double avgCpLoss) {
        // 0 cp loss = 100, 100+ cp loss = 0
        return Math.max(0, Math.min(100, 100 - avgCpLoss));
    }

    /**
     * Blend a new value into the existing stat using exponential moving average.
     */
    public double blendStat(double existing, double newValue) {
        return existing * (1 - WEIGHT) + newValue * WEIGHT;
    }
}
