package edu.fi.muni.cz.marketplace.order.events.handler;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.order.command.RegisterTrackingNumberCommand;
import edu.fi.muni.cz.marketplace.order.events.TrackingNumberSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingNumberSubmittedEventHandler {

  private final CommandGateway commandGateway;

  @EventHandler
  public void on(TrackingNumberSubmittedEvent event) {
    log.info("Tracking number submitted for order: {}, dispatching registration command",
        event.getOrderId());

    commandGateway.send(new RegisterTrackingNumberCommand(
        event.getOrderId(),
        event.getEnteredByUserId(),
        event.getTrackingNumber()));
  }
}
