package com.chesstune.core.service.fulfillment;

import com.chesstune.core.entity.Order;
import com.chesstune.core.entity.OrderItem;
import com.chesstune.core.enums.ProductType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Grants the user access to the individual mentor's dashboard.
 */
@Component
@Slf4j
public class MentorAccessFulfillment implements OrderFulfillmentStrategy {

    @Override
    public boolean supports(ProductType type) {
        return type == ProductType.INDIVIDUAL_MENTOR;
    }

    @Override
    public void fulfill(Order order, OrderItem item) {
        log.info("Granting user {} access to mentor from product {}",
                order.getUser().getUsername(), item.getProduct().getTitle());
        // In a full implementation: create MentorAccess record linking user to mentor
    }
}
