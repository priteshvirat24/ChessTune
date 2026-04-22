package com.chesstune.core.controller;

import com.chesstune.core.dto.PairingDTO;
import com.chesstune.core.dto.TournamentDTO;
import com.chesstune.core.entity.User;
import com.chesstune.core.repository.UserRepository;
import com.chesstune.core.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<TournamentDTO>> listTournaments() {
        return ResponseEntity.ok(tournamentService.getUpcomingAndActiveTournaments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentDTO> getTournament(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getTournamentDetails(id));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<Map<String, String>> register(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();
        tournamentService.registerUser(id, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Successfully registered for tournament"));
    }

    @GetMapping("/{id}/rounds/{round}")
    public ResponseEntity<List<PairingDTO>> getRoundPairings(
            @PathVariable Long id,
            @PathVariable Integer round) {
        return ResponseEntity.ok(tournamentService.getRoundPairings(id, round));
    }
}
