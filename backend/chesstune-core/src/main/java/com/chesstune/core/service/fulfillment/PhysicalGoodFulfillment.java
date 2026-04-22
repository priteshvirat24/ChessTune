package com.chesstune.core.service.fulfillment;

import com.chesstune.core.entity.Order;
import com.chesstune.core.entity.OrderItem;
import com.chesstune.core.enums.ProductType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Physical goods — marks as pending manual shipment.
 */
@Component
@Slf4j
public class PhysicalGoodFulfillment implements OrderFulfillmentStrategy {

    @Override
    public boolean supports(ProductType type) {
        return type == ProductType.PHYSICAL_GOOD;
    }

    @Override
    public void fulfill(Order order, OrderItem item) {
        log.info("Physical good '{}' for user {} — marked for manual shipment",
                item.getProduct().getTitle(), order.getUser().getUsername());
        // In a full implementation: create a shipping record
    }
}
