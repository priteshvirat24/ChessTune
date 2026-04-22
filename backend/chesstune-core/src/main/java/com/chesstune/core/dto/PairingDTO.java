package com.chesstune.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PairingDTO {

    private Long id;
    private Integer roundNumber;
    private Long whiteUserId;
    private String whiteUsername;
    private Long blackUserId;
    private String blackUsername;
    private String result;
    private Long gameId;
}
