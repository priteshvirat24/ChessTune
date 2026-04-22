package com.chesstune.core.service.fulfillment;

import com.chesstune.core.entity.Order;
import com.chesstune.core.entity.OrderItem;
import com.chesstune.core.enums.ProductType;

/**
 * Strategy interface for product-type-specific order fulfillment.
 */
public interface OrderFulfillmentStrategy {

    boolean supports(ProductType type);

    void fulfill(Order order, OrderItem item);
}
