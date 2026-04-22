package com.chesstune.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentDTO {

    private Long id;
    private String title;
    private LocalDateTime startTime;
    private String status;
    private String divisionAllowed;
    private Integer totalRounds;
    private Integer currentRound;
    private String timeControl;
    private Long participantCount;
    private List<ParticipantDTO> standings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantDTO {
        private Long userId;
        private String username;
        private Double score;
        private Double tiebreakScore;
        private Integer rank;
        private String division;
        private Integer rating;
    }
}
