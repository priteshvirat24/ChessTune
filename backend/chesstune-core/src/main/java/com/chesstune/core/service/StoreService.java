package com.chesstune.core.service;

import com.chesstune.core.dto.MentorDTO;
import com.chesstune.core.dto.ProductDTO;
import com.chesstune.core.entity.MentorProfile;
import com.chesstune.core.entity.Product;
import com.chesstune.core.entity.RPGStats;
import com.chesstune.core.enums.ProductType;
import com.chesstune.core.exception.ResourceNotFoundException;
import com.chesstune.core.repository.MentorProfileRepository;
import com.chesstune.core.repository.ProductRepository;
import com.chesstune.core.repository.RPGStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final ProductRepository productRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final RPGStatsRepository rpgStatsRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findByActiveTrueOrderByIdDesc()
                .stream().map(this::toProductDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByType(ProductType type) {
        return productRepository.findByProductTypeAndActiveTrueOrderByIdDesc(type)
                .stream().map(this::toProductDTO).collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return toProductDTO(product);
    }

    public List<MentorDTO> getAllMentors() {
        return mentorProfileRepository.findAll()
                .stream().map(this::toMentorDTO).collect(Collectors.toList());
    }

    private ProductDTO toProductDTO(Product p) {
        return ProductDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .price(p.getPrice())
                .productType(p.getProductType().name())
                .active(p.getActive())
                .linkedTournamentId(p.getLinkedTournamentId())
                .mentors(p.getMentors().stream().map(this::toMentorDTO).collect(Collectors.toList()))
                .build();
    }

    private MentorDTO toMentorDTO(MentorProfile m) {
        RPGStats stats = rpgStatsRepository.findByUserId(m.getUser().getId()).orElse(null);
        return MentorDTO.builder()
                .id(m.getId())
                .userId(m.getUser().getId())
                .username(m.getUser().getUsername())
                .bio(m.getBio())
                .achievements(m.getAchievements())
                .specialization(m.getSpecialization())
                .averageRating(m.getAverageRating())
                .openingAccuracy(stats != null ? stats.getOpeningAccuracy() : null)
                .tacticalVision(stats != null ? stats.getTacticalVision() : null)
                .endgameConversion(stats != null ? stats.getEndgameConversion() : null)
                .timeManagement(stats != null ? stats.getTimeManagement() : null)
                .build();
    }
}
