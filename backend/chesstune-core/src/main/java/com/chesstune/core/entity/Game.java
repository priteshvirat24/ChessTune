package com.chesstune.core.entity;

import com.chesstune.core.enums.GameResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "white_id", nullable = false)
    private User white;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "black_id", nullable = false)
    private User black;

    @Column(columnDefinition = "TEXT")
    private String pgn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GameResult result = GameResult.IN_PROGRESS;

    @Column(name = "time_control")
    @Builder.Default
    private String timeControl = "5+3";

    @Column(name = "is_tournament_game", nullable = false)
    @Builder.Default
    private Boolean isTournamentGame = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
