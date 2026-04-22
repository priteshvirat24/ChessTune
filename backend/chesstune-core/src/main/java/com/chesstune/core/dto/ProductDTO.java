package com.chesstune.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String productType;
    private Boolean active;
    private Long linkedTournamentId;
    private List<MentorDTO> mentors;
}
