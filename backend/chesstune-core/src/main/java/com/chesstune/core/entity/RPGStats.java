package com.chesstune.core.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * RPG-style stats tracking a player's chess skill dimensions.
 * All values are 0.0–100.0, initialized at 50.0.
 */
@Entity
@Table(name = "rpg_stats")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RPGStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "opening_accuracy", nullable = false)
    @Builder.Default
    private Double openingAccuracy = 50.0;

    @Column(name = "tactical_vision", nullable = false)
    @Builder.Default
    private Double tacticalVision = 50.0;

    @Column(name = "endgame_conversion", nullable = false)
    @Builder.Default
    private Double endgameConversion = 50.0;

    @Column(name = "time_management", nullable = false)
    @Builder.Default
    private Double timeManagement = 50.0;
}
