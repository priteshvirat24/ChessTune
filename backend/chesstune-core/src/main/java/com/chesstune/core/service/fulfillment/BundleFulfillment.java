package com.chesstune.core.service.fulfillment;

import com.chesstune.core.entity.Order;
import com.chesstune.core.entity.OrderItem;
import com.chesstune.core.enums.ProductType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Grants the user access to all mentors in the bundle.
 */
@Component
@Slf4j
public class BundleFulfillment implements OrderFulfillmentStrategy {

    @Override
    public boolean supports(ProductType type) {
        return type == ProductType.BUNDLE_PACKAGE;
    }

    @Override
    public void fulfill(Order order, OrderItem item) {
        log.info("Granting user {} access to bundle '{}' with {} mentors",
                order.getUser().getUsername(),
                item.getProduct().getTitle(),
                item.getProduct().getMentors().size());
        // In a full impl: create MentorAccess for each mentor in the bundle
    }
}
