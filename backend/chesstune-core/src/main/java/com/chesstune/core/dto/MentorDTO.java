package com.chesstune.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorDTO {

    private Long id;
    private Long userId;
    private String username;
    private String bio;
    private String achievements;
    private String specialization;
    private Double averageRating;

    // RPG Stats from the user
    private Double openingAccuracy;
    private Double tacticalVision;
    private Double endgameConversion;
    private Double timeManagement;
}
