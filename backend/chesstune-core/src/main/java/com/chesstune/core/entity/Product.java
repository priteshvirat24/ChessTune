package com.chesstune.core.entity;

import com.chesstune.core.enums.ProductType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType;

    @ManyToMany
    @JoinTable(
            name = "product_mentor_mapping",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "mentor_id")
    )
    @Builder.Default
    private List<MentorProfile> mentors = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /** For TOURNAMENT_TICKET products — linked tournament */
    @Column(name = "linked_tournament_id")
    private Long linkedTournamentId;
}
