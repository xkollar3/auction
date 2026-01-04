package edu.fi.muni.cz.marketplace.order.command.handler;

import edu.fi.muni.cz.marketplace.order.client.StripeFundsApiClient;
import edu.fi.muni.cz.marketplace.order.command.FinishRefundCommand;
import edu.fi.muni.cz.marketplace.order.command.RefundOrderCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundOrderCommandHandler {

  private final StripeFundsApiClient stripeFundsApiClient;
  private final CommandGateway commandGateway;

  @CommandHandler
  public void handle(RefundOrderCommand command) {
    log.info("Handling RefundOrderCommand for order: {}", command.getOrderId());

    String refundId = stripeFundsApiClient.refundPayment(command.getPaymentIntentId(),
        command.getOrderId());

    commandGateway.send(
        new FinishRefundCommand(command.getOrderId(), command.getPaymentIntentId()));
    log.info("Successfully refunded order {} with refund ID: {}", command.getOrderId(), refundId);
  }
}
