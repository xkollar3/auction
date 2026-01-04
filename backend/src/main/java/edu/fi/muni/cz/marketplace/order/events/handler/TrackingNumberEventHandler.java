package edu.fi.muni.cz.marketplace.order.events.handler;

import edu.fi.muni.cz.marketplace.order.command.AssignTrackingInfoCommand;
import edu.fi.muni.cz.marketplace.order.events.TrackingNumberProvidedEvent;
import edu.fi.muni.cz.marketplace.order.service.Ship24Service;
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

  private final Ship24Service ship24Service;
  private final CommandGateway commandGateway;

  @EventHandler
  public void on(TrackingNumberProvidedEvent event) {
    log.info("Creating Ship24 tracker for order: {}", event.getOrderId());

    String ship24TrackerId = ship24Service.createTracker(
        event.getTrackingNumber(),
        event.getOrderId().toString()
    );

    log.info("Ship24 tracker created with ID: {} for order: {}", ship24TrackerId,
        event.getOrderId());

    commandGateway.send(new AssignTrackingInfoCommand(
        event.getOrderId(),
        event.getTrackingNumber(),
        ship24TrackerId,
        Instant.now()
    ));
  }
}
