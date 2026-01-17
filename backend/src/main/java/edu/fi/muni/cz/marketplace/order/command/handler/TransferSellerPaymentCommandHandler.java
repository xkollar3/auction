package edu.fi.muni.cz.marketplace.order.command.handler;

import edu.fi.muni.cz.marketplace.order.client.StripeFundsApiClient;
import edu.fi.muni.cz.marketplace.order.client.StripeFundsApiClient.TransferType;
import edu.fi.muni.cz.marketplace.order.command.TransferSellerPayoutCommand;
import edu.fi.muni.cz.marketplace.order.events.SellerPayoutTransferredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferSellerPaymentCommandHandler {

  private final EventGateway eventGateway;
  private final StripeFundsApiClient stripeFundsApiClient;

  @CommandHandler
  public void on(TransferSellerPayoutCommand command) {
    String transferId = stripeFundsApiClient.transfer(command.getAmount(),
        command.getStripeAccountId(),
        command.getOrderId(),
        TransferType.PAYOUT);

    eventGateway.publish(new SellerPayoutTransferredEvent(command.getOrderId(), transferId));

    log.info("Funds transferred for order for order: {}", command.getOrderId());
  }
}
