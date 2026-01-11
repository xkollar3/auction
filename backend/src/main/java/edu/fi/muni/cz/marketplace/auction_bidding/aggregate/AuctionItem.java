package edu.fi.muni.cz.marketplace.auction_bidding.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import edu.fi.muni.cz.marketplace.auction_bidding.command.AddAuctionItemCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.command.CloseAuctionCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.command.PlaceBidCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.Bid;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.PlaceBidResponse;
import edu.fi.muni.cz.marketplace.auction_bidding.event.AuctionClosedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.AuctionItemAddedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.BidPlacedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.BidRejectedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.HighestBidSetEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

@Getter
@Setter
@Slf4j
@Aggregate
@NoArgsConstructor
public class AuctionItem {

  private static final String AUCTION_STARTING_PRICE_LOW = "Starting price cannot be negative";
  private static final String AUCTION_END_TIME_EXPIRED = "Auction end time can't be in the past";
  private static final String AUCTION_REJECT_REASON = "Bid amount is too low or auction closed already";
  private static final String AUCTION_END_DEADLINE = "auction-end-deadline";
  private static final int MAX_BIDS_SAVED = 10;

  @AggregateIdentifier
  private UUID id;

  private UUID sellerId; // keycloak user ID of the seller
  private String title;
  private String description;
  private BigDecimal startingPrice;
  private Instant auctionEndTime;
  private AuctionStatus status;

  // Current highest bid information
  private UUID highestBidderId; // keycloak user ID of the highest bidder
  private BigDecimal highestBidAmount;

  // List of recent bids (max 10), ordered by recency with highest bids at front
  private List<Bid> allBids = new LinkedList<>();

  /**
   * Command handler for creating a new auction item. Triggered by a Seller after their Stripe Connect account is
   * verified.
   */
  @CommandHandler
  public AuctionItem(AddAuctionItemCommand command, DeadlineManager manager) {
    if (command.getStartingPrice() == null || command.getStartingPrice().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException(AUCTION_STARTING_PRICE_LOW);
    }
    if (command.getAuctionEndTime().compareTo(Instant.now()) <= 0) {
      throw new IllegalArgumentException(AUCTION_END_TIME_EXPIRED);
    }

    apply(new AuctionItemAddedEvent(
        command.getAuctionItemId(),
        command.getSellerId(),
        command.getTitle(),
        command.getDescription(),
        command.getStartingPrice(),
        command.getAuctionEndTime()));
    manager.schedule(
        command.getAuctionEndTime(),
        AUCTION_END_DEADLINE,
        new CloseAuctionCommand(command.getAuctionItemId()));
  }

  @EventSourcingHandler
  public void on(AuctionItemAddedEvent event) {
    this.id = event.getAuctionItemId();
    this.sellerId = event.getSellerId();
    this.title = event.getTitle();
    this.description = event.getDescription();
    this.startingPrice = event.getStartingPrice();
    this.highestBidAmount = event.getStartingPrice();
    this.auctionEndTime = event.getAuctionEndTime();
    this.status = AuctionStatus.ACTIVE;
    log.info("Auction item {} created for seller {}", event.getAuctionItemId(), event.getSellerId());
  }

  /**
   * Command handler for placing a bid. Validates the bid and either accepts it (triggering SetHighestBid) or rejects it
   * (triggering RejectBid). Returns a result indicating whether the bid was accepted or rejected.
   */
  @CommandHandler
  public PlaceBidResponse handle(PlaceBidCommand command) {
    apply(new BidPlacedEvent(
        command.getAuctionItemId(),
        command.getBidId(),
        command.getBidderId(),
        command.getBidAmount()));

    if (status == AuctionStatus.ACTIVE && command.getBidAmount().compareTo(highestBidAmount) > 0) {
      apply(new HighestBidSetEvent(
          command.getAuctionItemId(),
          command.getBidderId(),
          command.getBidAmount()));
      return PlaceBidResponse.success();
    }

    apply(new BidRejectedEvent(
        command.getAuctionItemId(),
        command.getBidderId(),
        AUCTION_REJECT_REASON));
    return PlaceBidResponse.failure(AUCTION_REJECT_REASON);
  }

  @EventSourcingHandler
  public void on(BidPlacedEvent event) {
    if (event.getBidAmount().compareTo(highestBidAmount) <= 0) {
      log.info("Bid placed on auction item {} by bidder {}: {}. Bid was too low", event.getAuctionItemId(),
          event.getBidderId(), event.getBidAmount());
      return;
    }
    Bid newBid = new Bid(
        event.getBidId(),
        event.getBidderId(),
        event.getBidAmount());

    // Remove the bidder's previous bid if exists
    allBids.removeIf(bid -> bid.bidderId().equals(event.getBidderId()));

    // Add the new bid
    allBids.addFirst(newBid);

    // Sort by bid amount descending and keep only top 10
    if (allBids.size() > MAX_BIDS_SAVED) {
      allBids = allBids.subList(0, MAX_BIDS_SAVED);
    }

    log.info("Bid placed on auction item {} by bidder {}: {}. Total bids: {}",
        event.getAuctionItemId(), event.getBidderId(), event.getBidAmount(), allBids.size());
  }

  @EventSourcingHandler
  public void on(HighestBidSetEvent event) {
    this.highestBidderId = event.getBidderId();
    this.highestBidAmount = event.getBidAmount();
    log.info("New highest bid for auction item {} by bidder {}: {}",
        event.getAuctionItemId(), event.getBidderId(), event.getBidAmount());
  }

  @EventSourcingHandler
  public void on(BidRejectedEvent event) {
    log.info("Bid rejected for auction item {} of bidder {}: {}",
        event.getAuctionItemId(), event.getBidderId(), event.getReason());
    // No state change needed for rejected bids
  }

  @CommandHandler
  public void handleCloseAuctionCommand(CloseAuctionCommand command) {
    if (status == AuctionStatus.CLOSED) {
      log.info("Auction closed for auction item {}", command.getAuctionItemId());
      return;
    }
    if (auctionEndTime.isAfter(Instant.now())) {
      log.info("Auction item {} close attempt before deadline", command.getAuctionItemId());
    }
    apply(new AuctionClosedEvent(id, allBids));
  }

  @DeadlineHandler(deadlineName = AUCTION_END_DEADLINE)
  public void onAuctionEndDeadline(CloseAuctionCommand payload) {
    log.info("Auction end deadline reached for auction item ID: {}", payload.getAuctionItemId());
    if (status != AuctionStatus.CLOSED) {
      apply(new AuctionClosedEvent(id, allBids));
    }
  }

  @EventSourcingHandler
  public void on(AuctionClosedEvent event) {
    this.status = AuctionStatus.CLOSED;
    log.info("Auction item {} closed", event.getAuctionItemId());
  }
}
