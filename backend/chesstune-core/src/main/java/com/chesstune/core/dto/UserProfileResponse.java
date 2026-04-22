package com.chesstune.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String division;
    private Integer contestRating;

    // RPG Stats
    private Double openingAccuracy;
    private Double tacticalVision;
    private Double endgameConversion;
    private Double timeManagement;

    private Long pendingUpsolveTasks;
}
