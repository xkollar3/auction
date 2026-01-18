package edu.fi.muni.cz.marketplace.settlement.events.handler;

import edu.fi.muni.cz.marketplace.auction_bidding.event.AuctionClosedEvent;
import edu.fi.muni.cz.marketplace.settlement.aggregate.PotentialBuyer;
import edu.fi.muni.cz.marketplace.settlement.command.SelectBuyerCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionClosedEventHandler {

  private final CommandGateway commandGateway;

  @EventHandler
  public void on(AuctionClosedEvent event) {
    log.info("AuctionClosedEvent AuctionItem ID: {}", event.getAuctionItemId());
    List<PotentialBuyer> potentialBuyerList = event.getWinningBids() == null
        ? new ArrayList<>()
        : new ArrayList<>(event.getWinningBids().stream()
            .map(bid -> new PotentialBuyer(bid.getBidderId(), bid.getBidAmount()))
            .toList());
    commandGateway.send(new SelectBuyerCommand(UUID.randomUUID(), event.getAuctionItemId(), potentialBuyerList,
        event.getSellerId(), event.getTitle()));
  }

}
