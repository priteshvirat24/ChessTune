package com.chesstune.core.controller;

import com.chesstune.core.dto.*;
import com.chesstune.core.entity.User;
import com.chesstune.core.enums.ProductType;
import com.chesstune.core.repository.UserRepository;
import com.chesstune.core.service.OrderService;
import com.chesstune.core.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> getProducts(
            @RequestParam(required = false) String type) {
        if (type != null) {
            return ResponseEntity.ok(storeService.getProductsByType(ProductType.valueOf(type)));
        }
        return ResponseEntity.ok(storeService.getAllProducts());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.getProductById(id));
    }

    @GetMapping("/mentors")
    public ResponseEntity<List<MentorDTO>> getMentors() {
        return ResponseEntity.ok(storeService.getAllMentors());
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderDTO> checkout(
            @Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.checkout(user.getId(), request));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(orderService.getUserOrders(user.getId()));
    }
}
