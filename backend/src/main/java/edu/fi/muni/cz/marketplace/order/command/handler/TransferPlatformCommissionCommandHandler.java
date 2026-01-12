package edu.fi.muni.cz.marketplace.order.command.handler;

import edu.fi.muni.cz.marketplace.order.client.StripeFundsApiClient;
import edu.fi.muni.cz.marketplace.order.client.StripeFundsApiClient.TransferType;
import edu.fi.muni.cz.marketplace.order.command.TransferPlatformCommissionCommand;
import edu.fi.muni.cz.marketplace.order.events.PlatformCommissionTransferredEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransferPlatformCommissionCommandHandler {

  private final EventGateway eventGateway;
  private final StripeFundsApiClient stripeFundsApiClient;
  private final String platformAccountId;

  @Autowired
  public TransferPlatformCommissionCommandHandler(EventGateway eventGateway,
                                                  StripeFundsApiClient stripeFundsApiClient,
                                                  @Value("${stripe.platform-account-id}") String platformAccountId) {
    this.eventGateway = eventGateway;
    this.stripeFundsApiClient = stripeFundsApiClient;
    this.platformAccountId = platformAccountId;
  }

  @CommandHandler
  public void on(TransferPlatformCommissionCommand command) {
    String transferId = stripeFundsApiClient.transfer(command.getCommision(), platformAccountId,
        command.getOrderId(),
        TransferType.COMMISSION);

    eventGateway.publish(new PlatformCommissionTransferredEvent(command.getOrderId(), transferId));

    log.info("Commission deducted for order: {}", command.getOrderId());
  }
}
