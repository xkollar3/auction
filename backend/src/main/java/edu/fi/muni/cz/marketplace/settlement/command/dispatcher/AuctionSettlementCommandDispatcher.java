package edu.fi.muni.cz.marketplace.settlement.command.dispatcher;

import edu.fi.muni.cz.marketplace.order.command.ReserveFundsCommand;
import edu.fi.muni.cz.marketplace.settlement.command.SelectNextBuyerCommand;
import edu.fi.muni.cz.marketplace.settlement.events.BuyerSelectedEvent;
import edu.fi.muni.cz.marketplace.settlement.events.PurchaseConfirmedEvent;
import edu.fi.muni.cz.marketplace.settlement.events.PurchaseRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionSettlementCommandDispatcher {

    private final CommandGateway commandGateway;

    @EventHandler
    public void on(BuyerSelectedEvent event) {
        commandGateway.send(new ReserveFundsCommand(
                UUID.randomUUID(),
                event.getSelectedPotentialBuyer().bidderId().toString(),
                "paymentMethodId",
                event.getSelectedPotentialBuyer().bidAmount(),
                event.getSellerId(),
                "sellerStripeAccountId"
        ));
    }

    @EventHandler
    public void on(PurchaseConfirmedEvent event) {
        commandGateway.send(new ReserveFundsCommand(UUID.randomUUID(), event.getPotentialBuyer().bidderId().toString(), "paymentMethodId", event.getPotentialBuyer().bidAmount(), event.getSellerId(), "sellerStripeAccountId"));
    }

    @EventHandler
    public void on(PurchaseRejectedEvent event) {
         commandGateway.send(new SelectNextBuyerCommand(
             event.getSettlementId()
         ));
    }
}
