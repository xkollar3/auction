package edu.fi.muni.cz.marketplace.order.events.handler;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.order.command.AssignFundReservationInformationCommand;
import edu.fi.muni.cz.marketplace.order.events.FundsReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FundsReservedSuccessfullyEventHandler {

  private final CommandGateway commandGateway;

  @CommandHandler
  public void on(FundsReservedEvent event) {

    commandGateway.send(new AssignFundReservationInformationCommand(
        event.getOrderId(),
        event.getBuyerId(),
        event.getPaymentIntentId(),
        event.getPaymentMethodId(),
        event.getGrossAmount(),
        event.getReservedAt(),
        event.getSellerId(),
        event.getSellerStripeAccountId()));

    log.debug("Policy to assign fund reservation fired: {}", event.getOrderId());
  }
}
