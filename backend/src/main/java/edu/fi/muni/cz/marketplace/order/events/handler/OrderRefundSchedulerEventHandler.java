package edu.fi.muni.cz.marketplace.order.events.handler;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.order.command.RefundOrderCommand;
import edu.fi.muni.cz.marketplace.order.events.OrderRefundScheduledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRefundSchedulerEventHandler {

  private final CommandGateway commandGateway;

  @EventHandler
  public void on(OrderRefundScheduledEvent event) {
    log.info("Refund scheduled for order: {}", event.getOrderId());
    commandGateway.send(new RefundOrderCommand(event.getOrderId(), event.getPaymentIntentId()));
  }
}
