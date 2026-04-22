package com.chesstune.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mentor_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MentorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 500)
    private String achievements;

    @Column(length = 200)
    private String specialization;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @ManyToMany(mappedBy = "mentors")
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}
