package com.chesstune.core.controller;

import com.chesstune.core.dto.MentorDTO;
import com.chesstune.core.dto.OrderDTO;
import com.chesstune.core.dto.ProductDTO;
import com.chesstune.core.dto.TournamentDTO;
import com.chesstune.core.entity.*;
import com.chesstune.core.enums.Division;
import com.chesstune.core.enums.ProductType;
import com.chesstune.core.exception.ResourceNotFoundException;
import com.chesstune.core.repository.*;
import com.chesstune.core.service.OrderService;
import com.chesstune.core.service.TournamentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin-only endpoints for managing tournaments, products, mentors, orders, and users.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TournamentService tournamentService;
    private final TournamentRepository tournamentRepository;
    private final ProductRepository productRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final RPGStatsRepository rpgStatsRepository;

    // ─── Tournament Management ──────────────────────────

    @PostMapping("/tournaments")
    public ResponseEntity<TournamentDTO> createTournament(@RequestBody CreateTournamentRequest req) {
        Tournament tournament = Tournament.builder()
                .title(req.getTitle())
                .startTime(req.getStartTime())
                .divisionAllowed(req.getDivisionAllowed() != null
                        ? Division.valueOf(req.getDivisionAllowed()) : null)
                .totalRounds(req.getTotalRounds() != null ? req.getTotalRounds() : 5)
                .timeControl(req.getTimeControl() != null ? req.getTimeControl() : "5+3")
                .build();
        tournament = tournamentService.createTournament(tournament);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tournamentService.getTournamentDetails(tournament.getId()));
    }

    @PutMapping("/tournaments/{id}")
    public ResponseEntity<TournamentDTO> updateTournament(
            @PathVariable Long id, @RequestBody CreateTournamentRequest req) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));
        if (req.getTitle() != null) tournament.setTitle(req.getTitle());
        if (req.getStartTime() != null) tournament.setStartTime(req.getStartTime());
        if (req.getTotalRounds() != null) tournament.setTotalRounds(req.getTotalRounds());
        if (req.getTimeControl() != null) tournament.setTimeControl(req.getTimeControl());
        if (req.getDivisionAllowed() != null)
            tournament.setDivisionAllowed(Division.valueOf(req.getDivisionAllowed()));
        tournamentRepository.save(tournament);
        return ResponseEntity.ok(tournamentService.getTournamentDetails(id));
    }

    @DeleteMapping("/tournaments/{id}/kick/{userId}")
    public ResponseEntity<Map<String, String>> kickPlayer(
            @PathVariable Long id, @PathVariable Long userId) {
        tournamentService.kickPlayer(id, userId);
        return ResponseEntity.ok(Map.of("message", "Player kicked"));
    }

    @PostMapping("/tournaments/{id}/pair")
    public ResponseEntity<Map<String, String>> triggerPairing(@PathVariable Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));
        if (tournament.getCurrentRound() == 0) {
            tournamentService.startTournament(id);
        } else {
            tournamentService.advanceRound(id);
        }
        return ResponseEntity.ok(Map.of("message", "Round pairings generated"));
    }

    // ─── Product Management ─────────────────────────────

    @PostMapping("/products")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody CreateProductRequest req) {
        Product product = Product.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .price(req.getPrice())
                .productType(ProductType.valueOf(req.getProductType()))
                .linkedTournamentId(req.getLinkedTournamentId())
                .build();
        product = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(toProductDTO(product));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id, @RequestBody CreateProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (req.getTitle() != null) product.setTitle(req.getTitle());
        if (req.getDescription() != null) product.setDescription(req.getDescription());
        if (req.getPrice() != null) product.setPrice(req.getPrice());
        if (req.getActive() != null) product.setActive(req.getActive());
        product = productRepository.save(product);
        return ResponseEntity.ok(toProductDTO(product));
    }

    @PutMapping("/products/{id}/mentors")
    public ResponseEntity<ProductDTO> attachMentors(
            @PathVariable Long id, @RequestBody List<Long> mentorIds) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        List<MentorProfile> mentors = mentorProfileRepository.findAllById(mentorIds);
        product.setMentors(mentors);
        product = productRepository.save(product);
        return ResponseEntity.ok(toProductDTO(product));
    }

    // ─── Mentor Management ──────────────────────────────

    @PostMapping("/mentors")
    public ResponseEntity<MentorDTO> createMentor(@RequestBody CreateMentorRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(com.chesstune.core.enums.Role.MENTOR);
        userRepository.save(user);

        MentorProfile profile = MentorProfile.builder()
                .user(user)
                .bio(req.getBio())
                .achievements(req.getAchievements())
                .specialization(req.getSpecialization())
                .build();
        profile = mentorProfileRepository.save(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(toMentorDTO(profile));
    }

    @PutMapping("/mentors/{id}")
    public ResponseEntity<MentorDTO> updateMentor(
            @PathVariable Long id, @RequestBody CreateMentorRequest req) {
        MentorProfile profile = mentorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));
        if (req.getBio() != null) profile.setBio(req.getBio());
        if (req.getAchievements() != null) profile.setAchievements(req.getAchievements());
        if (req.getSpecialization() != null) profile.setSpecialization(req.getSpecialization());
        profile = mentorProfileRepository.save(profile);
        return ResponseEntity.ok(toMentorDTO(profile));
    }

    @GetMapping("/mentors")
    public ResponseEntity<List<MentorDTO>> listMentors() {
        return ResponseEntity.ok(mentorProfileRepository.findAll()
                .stream().map(this::toMentorDTO).collect(Collectors.toList()));
    }

    // ─── Order Management ───────────────────────────────

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> listOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/orders/{id}/fulfill")
    public ResponseEntity<OrderDTO> fulfillOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.fulfillOrder(id));
    }

    // ─── User Management ────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> listUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "email", u.getEmail(),
                        "role", u.getRole().name(),
                        "division", u.getCurrentDivision().name(),
                        "rating", u.getContestRating()
                )).collect(Collectors.toList()));
    }

    // ─── Request DTOs ───────────────────────────────────

    @Data
    public static class CreateTournamentRequest {
        private String title;
        private LocalDateTime startTime;
        private String divisionAllowed;
        private Integer totalRounds;
        private String timeControl;
    }

    @Data
    public static class CreateProductRequest {
        private String title;
        private String description;
        private BigDecimal price;
        private String productType;
        private Long linkedTournamentId;
        private Boolean active;
    }

    @Data
    public static class CreateMentorRequest {
        private Long userId;
        private String bio;
        private String achievements;
        private String specialization;
    }

    // ─── Helpers ────────────────────────────────────────

    private ProductDTO toProductDTO(Product p) {
        return ProductDTO.builder()
                .id(p.getId()).title(p.getTitle()).description(p.getDescription())
                .price(p.getPrice()).productType(p.getProductType().name())
                .active(p.getActive()).linkedTournamentId(p.getLinkedTournamentId())
                .mentors(p.getMentors().stream().map(this::toMentorDTO).collect(Collectors.toList()))
                .build();
    }

    private MentorDTO toMentorDTO(MentorProfile m) {
        RPGStats stats = rpgStatsRepository.findByUserId(m.getUser().getId()).orElse(null);
        return MentorDTO.builder()
                .id(m.getId()).userId(m.getUser().getId()).username(m.getUser().getUsername())
                .bio(m.getBio()).achievements(m.getAchievements())
                .specialization(m.getSpecialization()).averageRating(m.getAverageRating())
                .openingAccuracy(stats != null ? stats.getOpeningAccuracy() : null)
                .tacticalVision(stats != null ? stats.getTacticalVision() : null)
                .endgameConversion(stats != null ? stats.getEndgameConversion() : null)
                .timeManagement(stats != null ? stats.getTimeManagement() : null)
                .build();
    }
}
