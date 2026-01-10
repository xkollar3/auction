package edu.fi.muni.cz.marketplace.auction_bidding.saga;

import edu.fi.muni.cz.marketplace.auction_bidding.command.CloseAuctionCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.event.AuctionClosedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.AuctionItemAddedEvent;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
@Slf4j
public class AuctionManagementSaga {

  private static final String AUCTION_DEADLINE_NAME = "auction-end-deadline";

  @Autowired
  private transient CommandGateway commandGateway;

  @StartSaga
  @SagaEventHandler(associationProperty = "auctionItemId")
  public void on(AuctionItemAddedEvent event, DeadlineManager deadlineManager) {
    log.info("Registering auction end deadline for auction item ID: {} at {}",
        event.auctionItemId(), event.auctionEndTime());
    deadlineManager.schedule(
        event.auctionEndTime(),
        AUCTION_DEADLINE_NAME,
        event.auctionItemId());
  }

  @DeadlineHandler(deadlineName = AUCTION_DEADLINE_NAME)
  public void onAuctionEndDeadline(UUID auctionItemId) {
    log.info("Auction end deadline reached for auction item ID: {}", auctionItemId);
    commandGateway.send(new CloseAuctionCommand(auctionItemId));
  }

  @EndSaga
  @SagaEventHandler(associationProperty = "auctionItemId")
  public void on(AuctionClosedEvent event) {
    log.info("Auction closed for auction item ID: {}", event.auctionItemId());
    // Saga ends when auction is closed
  }


}
