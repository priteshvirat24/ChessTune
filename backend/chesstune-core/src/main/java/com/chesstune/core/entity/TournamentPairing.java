package com.chesstune.core.entity;

import com.chesstune.core.enums.GameResult;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tournament_pairings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TournamentPairing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private TournamentRound round;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "white_user_id", nullable = false)
    private User whiteUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "black_user_id")
    private User blackUser; // null = bye

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GameResult result = GameResult.IN_PROGRESS;

    @Column(name = "game_id")
    private Long gameId;
}
