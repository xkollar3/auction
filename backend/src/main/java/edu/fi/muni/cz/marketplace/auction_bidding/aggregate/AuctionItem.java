package edu.fi.muni.cz.marketplace.auction_bidding.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import edu.fi.muni.cz.marketplace.auction_bidding.command.AddAuctionItemCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.command.PlaceBidCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.Bid;
import edu.fi.muni.cz.marketplace.auction_bidding.event.AuctionClosedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.AuctionItemAddedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.BidPlacedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.BidRejectedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.HighestBidSetEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

@Getter
@Setter
@Slf4j
@Aggregate
@NoArgsConstructor
public class AuctionItem {

  private static final String AUCTION_CLOSED_REASON = "Auction is closed";
  private static final String BID_TOO_LOW_REASON = "Bid amount is too low";

  @AggregateIdentifier
  private UUID id;

  private UUID sellerId; // keycloak user ID of the seller
  private String title;
  private String description;
  private BigDecimal startingPrice;
  private Instant auctionEndTime;
  private AuctionStatus status;

  // Current highest bid information
  private UUID highestBidId;
  private UUID highestBidderId; // keycloak user ID of the highest bidder
  private BigDecimal highestBidAmount;

  // List of recent bids (max 10), ordered by recency with highest bids at front
  private List<Bid> allBids = new ArrayList<>();

  /**
   * Command handler for creating a new auction item. Triggered by a Seller after their Stripe Connect account is
   * verified.
   */
  @CommandHandler
  public AuctionItem(AddAuctionItemCommand command) {
    apply(new AuctionItemAddedEvent(
        command.auctionItemId(),
        command.sellerId(),
        command.startingPrice(),
        command.auctionEndTime()));
  }

  @EventSourcingHandler
  public void on(AuctionItemAddedEvent event) {
    this.id = event.auctionItemId();
    this.sellerId = event.sellerId();
    this.startingPrice = event.startingPrice();
    this.highestBidAmount = event.startingPrice();
    this.status = AuctionStatus.ACTIVE;
    log.info("Auction item {} created for seller {}", event.auctionItemId(), event.sellerId());
  }

  /**
   * Command handler for placing a bid. Validates the bid and either accepts it (triggering SetHighestBid) or rejects it
   * (triggering RejectBid).
   */
  @CommandHandler
  public void handle(PlaceBidCommand command) {
    apply(new BidPlacedEvent(
        command.auctionItemId(),
        command.bidderId(),
        command.bidAmount()));

    if (status == AuctionStatus.CLOSED) {
      apply(new BidRejectedEvent(
          command.auctionItemId(),
          command.bidderId(),
          AUCTION_CLOSED_REASON));
      throw new IllegalStateException(AUCTION_CLOSED_REASON);
    }

    if (command.bidAmount().compareTo(highestBidAmount) <= 0) {
      apply(new BidRejectedEvent(
          command.auctionItemId(),
          command.bidderId(),
          BID_TOO_LOW_REASON));
      throw new IllegalStateException(BID_TOO_LOW_REASON);
    }

    apply(new HighestBidSetEvent(
        command.auctionItemId(),
        command.bidderId(),
        command.bidAmount()));
  }

  @EventSourcingHandler
  public void on(BidPlacedEvent event) {
    Bid newBid = new Bid(
        UUID.randomUUID(),
        event.bidderId(),
        event.bidAmount());

    // add the new bid to the front list whilst removing the bidder's previous bid if exists
    allBids.removeIf(bid -> bid.bidderId().equals(event.bidderId()));
    allBids.addFirst(newBid);
    log.info("Bid placed on auction item {} by bidder {}: {}",
        event.auctionItemId(), event.bidderId(), event.bidAmount());
  }

  /**
   * Event sourcing handler for when an auction item is added.
   *
   * @param event the HighestBidSetEvent
   */
  @EventSourcingHandler
  public void on(HighestBidSetEvent event) {
    this.highestBidderId = event.bidderId();
    this.highestBidAmount = event.bidAmount();
    log.info("New highest bid for auction item {} by bidder {}: {}",
        event.auctionItemId(), event.bidderId(), event.bidAmount());
  }

  @EventSourcingHandler
  public void on(BidRejectedEvent event) {
    log.info("Bid rejected for auction item {} of bidder {}: {}",
        event.auctionItemId(), event.bidderId(), event.reason());
    // No state change needed for rejected bids
  }

  @CommandHandler
  public void handleCloseAuctionCommand() {
    apply(new AuctionClosedEvent(id, allBids));
  }

  @EventSourcingHandler
  public void on(AuctionClosedEvent event) {
    this.status = AuctionStatus.CLOSED;
    log.info("Auction item {} closed", event.auctionItemId());
  }
}
