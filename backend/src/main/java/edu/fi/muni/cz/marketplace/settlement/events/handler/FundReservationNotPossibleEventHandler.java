package edu.fi.muni.cz.marketplace.settlement.events.handler;

import edu.fi.muni.cz.marketplace.order.events.FundReservationNotPossibleEvent;
import edu.fi.muni.cz.marketplace.settlement.command.SelectBackupBuyerCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FundReservationNotPossibleEventHandler {

  private final CommandGateway commandGateway;

  @EventHandler
  public void on(FundReservationNotPossibleEvent event) {
    log.info("Fund reservation failed for buyer: {}, settlement: {}, reason: {}. Selecting next buyer.",
        event.getBuyerId(), event.getSettlementId(), event.getReason());

    commandGateway.send(new SelectBackupBuyerCommand(event.getSettlementId()));
  }
}
