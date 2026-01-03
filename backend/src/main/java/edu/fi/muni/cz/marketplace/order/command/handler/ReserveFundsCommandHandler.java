package edu.fi.muni.cz.marketplace.order.command.handler;

import java.time.Instant;

import edu.fi.muni.cz.marketplace.order.command.AssignFundReservationCommand;
import edu.fi.muni.cz.marketplace.order.command.ReserveFundsCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.order.client.StripeFundsApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReserveFundsCommandHandler {

  private final StripeFundsApiClient stripeFundsApiClient;
  private final CommandGateway commandGateway;

  @CommandHandler
  public void on(ReserveFundsCommand command) {
    log.info("Handling ReserveFundsCommand for order: {}", command.getId());

    String paymentIntentId = stripeFundsApiClient.reserveFunds(
        command.getCustomerId(),
        command.getPaymentMethodId(),
        command.getAmount(),
        command.getId());

    log.debug("Successfully reserved funds on Stripe. PaymentIntent: {}", paymentIntentId);

    commandGateway.sendAndWait(new AssignFundReservationCommand(
        command.getId(),
        paymentIntentId,
        command.getPaymentMethodId(),
        command.getAmount(),
        Instant.now(),
        command.getSellerId(),
        command.getSellerStripeAccountId()));

    log.debug("Successfully assigned fund reservation to order: {}", command.getId());
  }
}
