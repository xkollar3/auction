package edu.fi.muni.cz.marketplace.order.command.handler;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.order.command.RegisterTrackingNumberCommand;
import edu.fi.muni.cz.marketplace.order.events.TrackingNumberEnteredEvent;
import edu.fi.muni.cz.marketplace.order.service.Ship24Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterTrackingNumberCommandHandler {

  private final Ship24Service ship24Service;
  private final EventGateway eventGateway;

  @CommandHandler
  public void on(RegisterTrackingNumberCommand command) {
    log.info("Registering tracking number with Ship24 for order: {}", command.getOrderId());

    String ship24TrackerId = ship24Service.createTracker(
        command.getTrackingNumber(),
        command.getOrderId().toString());

    log.info("Ship24 tracker created with ID: {} for order: {}",
        ship24TrackerId, command.getOrderId());

    eventGateway.publish(new TrackingNumberEnteredEvent(
        command.getOrderId(),
        command.getEnteredByUserId(),
        command.getTrackingNumber(),
        ship24TrackerId));
  }
}
