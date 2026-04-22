package com.chesstune.core.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {

    @NotEmpty(message = "Cart cannot be empty")
    private List<CartItem> items;

    @Data
    public static class CartItem {
        private Long productId;
        private Integer quantity;
    }
}
