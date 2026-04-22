package com.chesstune.core.service.fulfillment;

import com.chesstune.core.entity.Order;
import com.chesstune.core.entity.OrderItem;
import com.chesstune.core.enums.ProductType;
import com.chesstune.core.service.TournamentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Auto-registers the user for the linked tournament.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TournamentTicketFulfillment implements OrderFulfillmentStrategy {

    private final TournamentService tournamentService;

    @Override
    public boolean supports(ProductType type) {
        return type == ProductType.TOURNAMENT_TICKET;
    }

    @Override
    public void fulfill(Order order, OrderItem item) {
        Long tournamentId = item.getProduct().getLinkedTournamentId();
        if (tournamentId != null) {
            try {
                tournamentService.registerUser(tournamentId, order.getUser().getId());
                log.info("Auto-registered user {} for tournament {}",
                        order.getUser().getUsername(), tournamentId);
            } catch (Exception e) {
                log.warn("Could not auto-register for tournament: {}", e.getMessage());
            }
        }
    }
}
