package edu.fi.muni.cz.marketplace.auction_bidding.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import edu.fi.muni.cz.marketplace.auction_bidding.command.AddAuctionItemCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.command.CloseAuctionCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.command.PlaceBidCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.dto.PlaceBidResponse;
import edu.fi.muni.cz.marketplace.auction_bidding.event.AuctionClosedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.AuctionItemAddedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.BidPlacedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.BidRejectedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.HighestBidSetEvent;
import edu.fi.muni.cz.marketplace.auction_item.command.AddImagesToAuctionCommand;
import edu.fi.muni.cz.marketplace.auction_item.event.ImagesAddedToAuctionEvent;
import jakarta.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
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

  @Nonnull
  @AggregateIdentifier
  private UUID id;

  @Nonnull
  private UUID sellerId; // keycloak user ID of the seller
  @Nonnull
  private String title;
  @Nonnull
  private String description;
  @Nonnull
  private BigDecimal startingPrice;
  @Nonnull
  private AuctionItemCategory category;
  @Nonnull
  private Instant auctionEndTime;
  @Nonnull
  private AuctionStatus status;

  // Current highest bid information
  private UUID highestBidderId; // keycloak user ID of the highest bidder
  private BigDecimal highestBidAmount;

  // List of recent bids (max 10), ordered by recency with highest bids at front
  private List<Bid> allBids = new LinkedList<>();

  // Image URLs for the auction item
  private List<String> imageUrls = new ArrayList<>();

  /**
   * Command handler for creating a new auction item. Triggered by a Seller after
   * their Stripe Connect account is
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
        command.getSellerFirstName(),
        command.getSellerLastName(),
        command.getTitle(),
        command.getDescription(),
        command.getStartingPrice(),
        command.getCategory(),
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
    this.category = event.getCategory();
    this.auctionEndTime = event.getAuctionEndTime();
    this.status = AuctionStatus.ACTIVE;
    log.info("Auction item {} created for seller {}", event.getAuctionItemId(), event.getSellerId());
  }

  /**
   * Command handler for placing a bid. Validates the bid and either accepts it
   * (triggering SetHighestBid) or rejects it
   * (triggering RejectBid). Returns a result indicating whether the bid was
   * accepted or rejected.
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
          command.getBidId(),
          command.getBidderId(),
          command.getBidAmount(),
          Instant.now()));
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
    log.info("Bid placed on auction item {} by bidder {}: {}",
        event.getAuctionItemId(), event.getBidderId(), event.getBidAmount());
    // No state change - BidPlacedEvent is for analytics/tracking purposes only
  }

  @EventSourcingHandler
  public void on(HighestBidSetEvent event) {
    this.highestBidderId = event.getBidderId();
    this.highestBidAmount = event.getBidAmount();

    Bid newBid = new Bid(
        event.getBidId(),
        event.getBidderId(),
        event.getBidAmount());

    // Remove the bidder's previous bid if exists (each bidder can only have one bid
    // in top 10)
    allBids.removeIf(bid -> bid.bidderId().equals(event.getBidderId()));

    // Add the new highest bid at the front (bids are ordered by recency/amount)
    allBids.addFirst(newBid);

    // Keep only top 10 bids
    if (allBids.size() > MAX_BIDS_SAVED) {
      allBids = allBids.subList(0, MAX_BIDS_SAVED);
    }

    log.info("New highest bid for auction item {} by bidder {}: {}. Total tracked bids: {}",
        event.getAuctionItemId(), event.getBidderId(), event.getBidAmount(), allBids.size());
  }

  @EventSourcingHandler
  public void on(BidRejectedEvent event) {
    log.info("Bid rejected for auction item {} of bidder {}: {}",
        event.getAuctionItemId(), event.getBidderId(), event.getReason());
    // No state change needed for rejected bids
  }

  @DeadlineHandler(deadlineName = AUCTION_END_DEADLINE)
  public void onAuctionEndDeadline(CloseAuctionCommand payload) {
    log.info("Auction end deadline reached for auction item ID: {}", payload.getAuctionItemId());
    if (status != AuctionStatus.CLOSED) {
      apply(new AuctionClosedEvent(id, sellerId, title, allBids));
    }
  }

  @EventSourcingHandler
  public void on(AuctionClosedEvent event) {
    this.status = AuctionStatus.CLOSED;
    log.info("Auction item {} closed", event.getAuctionItemId());
  }

  @CommandHandler
  public void handle(AddImagesToAuctionCommand command) {
    log.info("Adding {} images to auction item {}", command.getImageUrls().size(), command.getAuctionItemId());
    apply(new ImagesAddedToAuctionEvent(command.getAuctionItemId(), command.getImageUrls()));
  }

  @EventSourcingHandler
  public void on(ImagesAddedToAuctionEvent event) {
    this.imageUrls.addAll(event.getImageUrls());
    log.info("Added {} images to auction item {}, total images: {}",
        event.getImageUrls().size(), event.getAuctionItemId(), this.imageUrls.size());
  }
}
