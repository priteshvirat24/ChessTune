package com.chesstune.core.service;

import com.chesstune.core.dto.CheckoutRequest;
import com.chesstune.core.dto.OrderDTO;
import com.chesstune.core.entity.*;
import com.chesstune.core.enums.OrderStatus;
import com.chesstune.core.exception.ResourceNotFoundException;
import com.chesstune.core.repository.*;
import com.chesstune.core.service.fulfillment.OrderFulfillmentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final List<OrderFulfillmentStrategy> fulfillmentStrategies;

    @Transactional
    public OrderDTO checkout(Long userId, CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        Order order = Order.builder()
                .user(user)
                .totalAmount(BigDecimal.ZERO)
                .build();

        for (CheckoutRequest.CartItem cartItem : request.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + cartItem.getProductId()));

            int qty = cartItem.getQuantity() != null ? cartItem.getQuantity() : 1;
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(lineTotal);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(qty)
                    .price(product.getPrice())
                    .build();
            items.add(item);
        }

        order.setTotalAmount(total);
        order.setItems(items);
        order = orderRepository.save(order);

        // Dispatch fulfillment strategies
        for (OrderItem item : order.getItems()) {
            for (OrderFulfillmentStrategy strategy : fulfillmentStrategies) {
                if (strategy.supports(item.getProduct().getProductType())) {
                    strategy.fulfill(order, item);
                    break;
                }
            }
        }

        // Mark as fulfilled
        order.setStatus(OrderStatus.FULFILLED);
        order = orderRepository.save(order);

        log.info("Order {} created for user {} — total: {}",
                order.getId(), user.getUsername(), total);

        return toDTO(order);
    }

    public List<OrderDTO> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO fulfillOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(OrderStatus.FULFILLED);
        return toDTO(orderRepository.save(order));
    }

    private OrderDTO toDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(i -> OrderDTO.OrderItemDTO.builder()
                                .productId(i.getProduct().getId())
                                .productTitle(i.getProduct().getTitle())
                                .quantity(i.getQuantity())
                                .price(i.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
