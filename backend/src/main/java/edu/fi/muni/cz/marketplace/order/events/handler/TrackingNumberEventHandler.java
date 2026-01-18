package edu.fi.muni.cz.marketplace.order.events.handler;

import edu.fi.muni.cz.marketplace.order.command.AssignTrackingNumberToOrderCommand;
import edu.fi.muni.cz.marketplace.order.events.TrackingNumberEnteredEvent;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingNumberEventHandler {

  private final CommandGateway commandGateway;

  @EventHandler
  public void on(TrackingNumberEnteredEvent event) {
    log.info("Assigning tracking number to order: {}", event.getOrderId());

    commandGateway.send(new AssignTrackingNumberToOrderCommand(
        event.getOrderId(),
        event.getEnteredByUserId(),
        event.getTrackingNumber(),
        event.getTrackerId(),
        Instant.now()));
  }
}
