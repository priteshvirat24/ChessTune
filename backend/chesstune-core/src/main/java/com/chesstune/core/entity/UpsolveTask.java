package com.chesstune.core.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * An upsolve task generated from a blunder detected by Stockfish.
 * Users must solve all tasks before registering for the next tournament.
 */
@Entity
@Table(name = "upsolve_tasks")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UpsolveTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "game_id")
    private Long gameId;

    @Column(name = "fen_state", nullable = false, length = 100)
    private String fenState;

    @Column(name = "correct_move", nullable = false, length = 10)
    private String correctMove;

    @Column(name = "user_mistake", nullable = false, length = 10)
    private String userMistake;

    @Column(name = "is_solved", nullable = false)
    @Builder.Default
    private Boolean isSolved = false;
}
