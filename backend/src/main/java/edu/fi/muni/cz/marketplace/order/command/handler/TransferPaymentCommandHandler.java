package edu.fi.muni.cz.marketplace.order.command.handler;

import edu.fi.muni.cz.marketplace.order.client.StripeFundsApiClient;
import edu.fi.muni.cz.marketplace.order.client.StripeFundsApiClient.TransferType;
import edu.fi.muni.cz.marketplace.order.command.TransferPaymentCommand;
import edu.fi.muni.cz.marketplace.order.events.PaymentTransferredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferPaymentCommandHandler {

  private final EventGateway eventGateway;
  private final StripeFundsApiClient stripeFundsApiClient;

  @CommandHandler
  public void on(TransferPaymentCommand command) {
    String transferId = stripeFundsApiClient.transfer(command.getAmount(),
        command.getStripeAccountId(),
        command.getOrderId(),
        TransferType.PAYOUT);

    eventGateway.publish(new PaymentTransferredEvent(command.getOrderId(), transferId));

    log.info("Funds transferred for order for order: {}", command.getOrderId());
  }
}
