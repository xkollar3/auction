package edu.fi.muni.cz.marketplace.order.events.handler;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.order.command.ReserveFundsCommand;
import edu.fi.muni.cz.marketplace.settlement.aggregate.PotentialBuyer;
import edu.fi.muni.cz.marketplace.settlement.events.BuyerSelectedEvent;
import edu.fi.muni.cz.marketplace.user.query.UserReadModel;
import edu.fi.muni.cz.marketplace.user.query.UserReadModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuyerSelectedEventHandler {

  private final CommandGateway commandGateway;
  private final UserReadModelRepository userReadModelRepository;

  @EventHandler
  public void on(BuyerSelectedEvent event) {
    PotentialBuyer selectedBuyer = event.getSelectedPotentialBuyer();

    UserReadModel buyerProfile = userReadModelRepository.findById(selectedBuyer.bidderId())
        .orElseThrow(() -> new IllegalStateException(
            "Buyer not found in read model: " + selectedBuyer.bidderId()));

    UserReadModel sellerProfile = userReadModelRepository.findById(event.getSellerId())
        .orElseThrow(() -> new IllegalStateException(
            "Seller not found in read model: " + event.getSellerId()));

    UUID orderId = UUID.randomUUID();

    commandGateway.send(new ReserveFundsCommand(
        orderId,
        event.getSettlementId(),
        selectedBuyer.bidderId(),
        buyerProfile.getStripeCustomerId(),
        buyerProfile.getStripePaymentMethodId(),
        selectedBuyer.bidAmount(),
        event.getSellerId(),
        sellerProfile.getStripeSellerAccountId()));

    log.debug("Policy to reserve funds fired for order: {}, buyer: {}, amount: {}",
        orderId, selectedBuyer.bidderId(), selectedBuyer.bidAmount());
  }
}
