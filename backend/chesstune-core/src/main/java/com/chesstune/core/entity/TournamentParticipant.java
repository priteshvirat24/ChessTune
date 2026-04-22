package com.chesstune.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tournament_participants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tournament_id", "user_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TournamentParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "current_score", nullable = false)
    @Builder.Default
    private Double currentScore = 0.0;

    @Column(name = "tiebreak_score", nullable = false)
    @Builder.Default
    private Double tiebreakScore = 0.0;

    @Column(name = "rank")
    private Integer rank;

    /** Tracks which color was assigned last for color alternation */
    @Column(name = "last_color")
    private String lastColor;
}
