package edu.fi.muni.cz.marketplace.auction_bidding.aggregate;

import static org.axonframework.test.matchers.Matchers.exactSequenceOf;
import static org.axonframework.test.matchers.Matchers.messageWithPayload;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.fi.muni.cz.marketplace.auction_bidding.command.AddAuctionItemCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.command.CloseAuctionCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.command.PlaceBidCommand;
import edu.fi.muni.cz.marketplace.auction_bidding.event.AuctionClosedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.AuctionItemAddedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.BidPlacedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.BidRejectedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.HighestBidSetEvent;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuctionItemTest {

  private static final Instant FIXED_TIME = Instant.parse("2026-01-10T12:00:00Z");
  private static final String AUCTION_END_DEADLINE = "auction-end-deadline";

  private FixtureConfiguration<AuctionItem> fixture;

  @BeforeEach
  void setUp() {
    fixture = new AggregateTestFixture<>(AuctionItem.class);
  }

  @Test
  void addAuctionItem_validCommand_shouldEmitEventScheduleDeadlineAndSetState() {
    UUID auctionItemId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    String title = "Test Auction Item";
    String description = "A description of the test item";
    BigDecimal startingPrice = new BigDecimal("100.00");
    Instant auctionEndTime = FIXED_TIME.plus(Duration.ofDays(7));

    fixture.givenCurrentTime(FIXED_TIME)
        .when(new AddAuctionItemCommand(
            auctionItemId,
            sellerId,
            title,
            description,
            startingPrice,
            auctionEndTime))
        .expectSuccessfulHandlerExecution()
        .expectEventsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(AuctionItemAddedEvent.class))))
        .expectScheduledDeadline(
            Duration.ofDays(7),
            new CloseAuctionCommand(auctionItemId))
        .expectState(auction -> {
          assertEquals(auctionItemId, auction.getId());
          assertEquals(sellerId, auction.getSellerId());
          assertEquals(title, auction.getTitle());
          assertEquals(description, auction.getDescription());
          assertEquals(startingPrice, auction.getStartingPrice());
          assertEquals(auctionEndTime, auction.getAuctionEndTime());
          assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
          assertEquals(startingPrice, auction.getHighestBidAmount());
        });
  }

  @Test
  void placeBid_auctionActiveAndBidHigherThanCurrent_shouldAcceptBidAndSetHighest() {
    UUID auctionItemId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    UUID bidderId = UUID.randomUUID();
    UUID bidId = UUID.randomUUID();
    String title = "Test Auction Item";
    String description = "A description";
    BigDecimal startingPrice = new BigDecimal("100.00");
    BigDecimal bidAmount = new BigDecimal("150.00");
    Instant auctionEndTime = FIXED_TIME.plus(Duration.ofDays(7));

    fixture.given(new AuctionItemAddedEvent(
            auctionItemId,
            sellerId,
            title,
            description,
            startingPrice,
            auctionEndTime))
        .when(new PlaceBidCommand(auctionItemId, bidId, bidderId, bidAmount))
        .expectSuccessfulHandlerExecution()
        .expectEventsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(BidPlacedEvent.class)),
            messageWithPayload(instanceOf(HighestBidSetEvent.class))))
        .expectState(auction -> {
          assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
          assertEquals(bidderId, auction.getHighestBidderId());
          assertEquals(bidAmount, auction.getHighestBidAmount());
          assertEquals(1, auction.getAllBids().size());
        });
  }

  @Test
  void placeBid_auctionActiveAndBidLowerThanCurrent_shouldRejectBidAndEmitEvents() {
    UUID auctionItemId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    UUID bidderId = UUID.randomUUID();
    UUID bidId = UUID.randomUUID();
    String title = "Test Auction Item";
    String description = "A description";
    BigDecimal startingPrice = new BigDecimal("100.00");
    BigDecimal bidAmount = new BigDecimal("50.00");
    Instant auctionEndTime = FIXED_TIME.plus(Duration.ofDays(7));

    fixture.given(new AuctionItemAddedEvent(
            auctionItemId,
            sellerId,
            title,
            description,
            startingPrice,
            auctionEndTime))
        .when(new PlaceBidCommand(auctionItemId, bidId, bidderId, bidAmount))
        .expectSuccessfulHandlerExecution()
        .expectEventsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(BidPlacedEvent.class)),
            messageWithPayload(instanceOf(BidRejectedEvent.class))))
        .expectState(auction -> {
          // Bid is recorded but not set as highest
          assertEquals(0, auction.getAllBids().size());
          assertEquals(startingPrice, auction.getHighestBidAmount());
        });
  }

  @Test
  void placeBid_auctionActiveAndBidEqualToCurrent_shouldRejectBid() {
    UUID auctionItemId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    UUID bidderId = UUID.randomUUID();
    UUID bidId = UUID.randomUUID();
    String title = "Test Auction Item";
    String description = "A description";
    BigDecimal startingPrice = new BigDecimal("100.00");
    BigDecimal bidAmount = new BigDecimal("100.00");
    Instant auctionEndTime = FIXED_TIME.plus(Duration.ofDays(7));

    fixture.given(new AuctionItemAddedEvent(
            auctionItemId,
            sellerId,
            title,
            description,
            startingPrice,
            auctionEndTime))
        .when(new PlaceBidCommand(auctionItemId, bidId, bidderId, bidAmount))
        .expectSuccessfulHandlerExecution()
        .expectEventsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(BidPlacedEvent.class)),
            messageWithPayload(instanceOf(BidRejectedEvent.class))))
        .expectState(auction -> {
          assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
          // Bid is recorded and bidderId remains null as no highest bid is set since no bid is higher than starting price
          assertNull(auction.getHighestBidderId());
          assertEquals(bidAmount, auction.getHighestBidAmount());
        });
  }

  @Test
  void placeBid_auctionClosed_shouldRejectBidAndEmitEvents() {
    UUID auctionItemId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    UUID bidderId = UUID.randomUUID();
    UUID bidId = UUID.randomUUID();
    String title = "Test Auction Item";
    String description = "A description";
    BigDecimal startingPrice = new BigDecimal("100.00");
    BigDecimal bidAmount = new BigDecimal("150.00");
    Instant auctionEndTime = FIXED_TIME.plus(Duration.ofDays(7));

    fixture.given(
            new AuctionItemAddedEvent(
                auctionItemId,
                sellerId,
                title,
                description,
                startingPrice,
                auctionEndTime),
            new AuctionClosedEvent(auctionItemId, Collections.emptyList()))
        .when(new PlaceBidCommand(auctionItemId, bidId, bidderId, bidAmount))
        .expectSuccessfulHandlerExecution()
        .expectEventsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(BidPlacedEvent.class)),
            messageWithPayload(instanceOf(BidRejectedEvent.class))))
        .expectState(auction -> assertEquals(AuctionStatus.CLOSED, auction.getStatus()));
  }

  @Test
  void placeBid_multipleBidsFromSameBidder_shouldReplaceOldBid() {
    UUID auctionItemId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    UUID bidderId = UUID.randomUUID();
    UUID bidId = UUID.randomUUID();
    String title = "Test Auction Item";
    String description = "A description";
    BigDecimal startingPrice = new BigDecimal("100.00");
    BigDecimal firstBidAmount = new BigDecimal("150.00");
    BigDecimal secondBidAmount = new BigDecimal("200.00");
    Instant auctionEndTime = FIXED_TIME.plus(Duration.ofDays(7));

    fixture.given(
            new AuctionItemAddedEvent(
                auctionItemId,
                sellerId,
                title,
                description,
                startingPrice,
                auctionEndTime),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderId, firstBidAmount),
            new HighestBidSetEvent(auctionItemId, bidderId, firstBidAmount))
        .when(new PlaceBidCommand(auctionItemId, bidId, bidderId, secondBidAmount))
        .expectSuccessfulHandlerExecution()
        .expectEventsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(BidPlacedEvent.class)),
            messageWithPayload(instanceOf(HighestBidSetEvent.class))))
        .expectState(auction -> {
          assertEquals(bidderId, auction.getHighestBidderId());
          assertEquals(secondBidAmount, auction.getHighestBidAmount());
          // Only one bid from the same bidder should be kept
          assertEquals(1, auction.getAllBids().size());
        });
  }

  @Test
  void closeAuction_auctionActive_shouldCloseAuctionAndEmitEvent() {
    UUID auctionItemId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    String title = "Test Auction Item";
    String description = "A description";
    BigDecimal startingPrice = new BigDecimal("100.00");
    Instant auctionEndTime = FIXED_TIME.plus(Duration.ofDays(7));

    fixture.givenCurrentTime(FIXED_TIME)
        .andGivenCommands(new AddAuctionItemCommand(
            auctionItemId,
            sellerId,
            title,
            description,
            startingPrice,
            auctionEndTime))
        .whenTimeElapses(Duration.ofDays(7))
        .expectTriggeredDeadlinesWithName(AUCTION_END_DEADLINE)
        .expectEventsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(AuctionClosedEvent.class))))
        .expectState(auction ->
            assertEquals(AuctionStatus.CLOSED, auction.getStatus()));
  }

  @Test
  void closeAuction_auctionAlreadyClosed_shouldNotEmitEvent() {
    UUID auctionItemId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    String title = "Test Auction Item";
    String description = "A description";
    BigDecimal startingPrice = new BigDecimal("100.00");
    Instant auctionEndTime = FIXED_TIME.plus(Duration.ofDays(7));

    // Given auction is created (which schedules deadline) and then manually closed via event,
    // when deadline fires, no new event should be emitted since auction is already closed
    fixture.givenCurrentTime(FIXED_TIME)
        .andGivenCommands(new AddAuctionItemCommand(
            auctionItemId,
            sellerId,
            title,
            description,
            startingPrice,
            auctionEndTime))
        .andGiven(new AuctionClosedEvent(auctionItemId, Collections.emptyList()))
        .whenTimeElapses(Duration.ofDays(7))
        .expectTriggeredDeadlinesWithName(AUCTION_END_DEADLINE)
        .expectNoEvents()
        .expectState(auction ->
            assertEquals(AuctionStatus.CLOSED, auction.getStatus()));
  }

  @Test
  void auctionEndDeadline_auctionActive_shouldCloseAuction() {
    UUID auctionItemId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    String title = "Test Auction Item";
    String description = "A description of the test item";
    BigDecimal startingPrice = new BigDecimal("100.00");
    Instant auctionEndTime = FIXED_TIME.plus(Duration.ofDays(7));

    fixture.givenCurrentTime(FIXED_TIME)
        .andGivenCommands(new AddAuctionItemCommand(
            auctionItemId,
            sellerId,
            title,
            description,
            startingPrice,
            auctionEndTime))
        .whenTimeElapses(Duration.ofDays(7))
        .expectTriggeredDeadlinesWithName(AUCTION_END_DEADLINE)
        .expectEventsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(AuctionClosedEvent.class))))
        .expectState(auction ->
            assertEquals(AuctionStatus.CLOSED, auction.getStatus()));
  }

  @Test
  void auctionEndDeadline_auctionAlreadyClosed_shouldNotEmitEvent() {
    UUID auctionItemId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    String title = "Test Auction Item";
    String description = "A description of the test item";
    BigDecimal startingPrice = new BigDecimal("100.00");
    Instant auctionEndTime = FIXED_TIME.plus(Duration.ofDays(7));

    // Given auction is created (which schedules deadline) and then manually closed via event,
    // when deadline fires, no new event should be emitted since auction is already closed
    fixture.givenCurrentTime(FIXED_TIME)
        .andGivenCommands(new AddAuctionItemCommand(
            auctionItemId,
            sellerId,
            title,
            description,
            startingPrice,
            auctionEndTime))
        .andGiven(new AuctionClosedEvent(auctionItemId, Collections.emptyList()))
        .whenTimeElapses(Duration.ofDays(7))
        .expectTriggeredDeadlinesWithName(AUCTION_END_DEADLINE)
        .expectNoEvents()
        .expectState(auction ->
            assertEquals(AuctionStatus.CLOSED, auction.getStatus()));
  }

  @Test
  void closeAuction_withBids_shouldIncludeBidsInEvent() {
    UUID auctionItemId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    UUID bidderId1 = UUID.randomUUID();
    UUID bidderId2 = UUID.randomUUID();
    String title = "Test Auction Item";
    String description = "A description";
    BigDecimal startingPrice = new BigDecimal("100.00");
    BigDecimal bidAmount1 = new BigDecimal("150.00");
    BigDecimal bidAmount2 = new BigDecimal("200.00");
    Instant auctionEndTime = FIXED_TIME.plus(Duration.ofDays(7));

    fixture.givenCurrentTime(FIXED_TIME)
        .andGivenCommands(new AddAuctionItemCommand(
            auctionItemId,
            sellerId,
            title,
            description,
            startingPrice,
            auctionEndTime))
        .andGiven(
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderId1, bidAmount1),
            new HighestBidSetEvent(auctionItemId, bidderId1, bidAmount1),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderId2, bidAmount2),
            new HighestBidSetEvent(auctionItemId, bidderId2, bidAmount2))
        .whenTimeElapses(Duration.ofDays(7))
        .expectTriggeredDeadlinesWithName(AUCTION_END_DEADLINE)
        .expectState(auction -> {
          assertEquals(AuctionStatus.CLOSED, auction.getStatus());
          assertEquals(2, auction.getAllBids().size());
          assertEquals(bidderId2, auction.getHighestBidderId());
          assertEquals(bidAmount2, auction.getHighestBidAmount());
        });
  }

  @Test
  void placeBid_moreThan10Bidders_shouldKeepOnlyTop10ByAmount() {
    UUID auctionItemId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    String title = "Test Auction Item";
    String description = "A description";
    BigDecimal startingPrice = new BigDecimal("100.00");
    Instant auctionEndTime = FIXED_TIME.plus(Duration.ofDays(7));

    // Create 12 bidders with increasing bid amounts
    UUID[] bidderIds = new UUID[12];
    for (int i = 0; i < 12; i++) {
      bidderIds[i] = UUID.randomUUID();
    }

    // Set up the auction with 10 initial bids (amounts 110-200)
    fixture.given(
            new AuctionItemAddedEvent(auctionItemId, sellerId, title, description, startingPrice, auctionEndTime),
            // Bids with amounts 110, 120, 130, ..., 200
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[0], new BigDecimal("110.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[0], new BigDecimal("110.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[1], new BigDecimal("120.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[1], new BigDecimal("120.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[2], new BigDecimal("130.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[2], new BigDecimal("130.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[3], new BigDecimal("140.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[3], new BigDecimal("140.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[4], new BigDecimal("150.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[4], new BigDecimal("150.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[5], new BigDecimal("160.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[5], new BigDecimal("160.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[6], new BigDecimal("170.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[6], new BigDecimal("170.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[7], new BigDecimal("180.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[7], new BigDecimal("180.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[8], new BigDecimal("190.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[8], new BigDecimal("190.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[9], new BigDecimal("200.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[9], new BigDecimal("200.00")))
        // Now place an 11th bid with a higher amount - should push out the lowest (110.00)
        .when(new PlaceBidCommand(auctionItemId, UUID.randomUUID(), bidderIds[10], new BigDecimal("210.00")))
        .expectSuccessfulHandlerExecution()
        .expectState(auction -> {
          assertEquals(10, auction.getAllBids().size());
          // The highest bid should be 210.00
          assertEquals(new BigDecimal("210.00"), auction.getHighestBidAmount());
          assertEquals(bidderIds[10], auction.getHighestBidderId());
          // The lowest bid (110.00 from bidderIds[0]) should be removed
          boolean lowestBidderPresent = auction.getAllBids().stream()
              .anyMatch(bid -> bid.bidderId().equals(bidderIds[0]));
          assertFalse(lowestBidderPresent, "Lowest bidder should be removed from top 10");
          // The second lowest (120.00 from bidderIds[1]) should still be present
          boolean secondLowestPresent = auction.getAllBids().stream()
              .anyMatch(bid -> bid.bidderId().equals(bidderIds[1]));
          assertTrue(secondLowestPresent, "Second lowest bidder should still be in top 10");
        });
  }

  @Test
  void placeBid_lowBidWith10Existing_shouldNotBeIncludedInTop10() {
    UUID auctionItemId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    String title = "Test Auction Item";
    String description = "A description";
    BigDecimal startingPrice = new BigDecimal("100.00");
    Instant auctionEndTime = FIXED_TIME.plus(Duration.ofDays(7));

    // Create 11 bidders
    UUID[] bidderIds = new UUID[11];
    for (int i = 0; i < 11; i++) {
      bidderIds[i] = UUID.randomUUID();
    }

    // Set up the auction with 10 bids (amounts 110-200)
    fixture.given(
            new AuctionItemAddedEvent(auctionItemId, sellerId, title, description, startingPrice, auctionEndTime),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[0], new BigDecimal("110.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[0], new BigDecimal("110.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[1], new BigDecimal("120.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[1], new BigDecimal("120.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[2], new BigDecimal("130.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[2], new BigDecimal("130.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[3], new BigDecimal("140.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[3], new BigDecimal("140.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[4], new BigDecimal("150.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[4], new BigDecimal("150.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[5], new BigDecimal("160.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[5], new BigDecimal("160.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[6], new BigDecimal("170.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[6], new BigDecimal("170.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[7], new BigDecimal("180.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[7], new BigDecimal("180.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[8], new BigDecimal("190.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[8], new BigDecimal("190.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderIds[9], new BigDecimal("200.00")),
            new HighestBidSetEvent(auctionItemId, bidderIds[9], new BigDecimal("200.00")))
        // Place an 11th bid with a LOWER amount than the lowest (105 < 110) - will be rejected and not in top 10 by default
        .when(new PlaceBidCommand(auctionItemId, UUID.randomUUID(), bidderIds[10], new BigDecimal("105.00")))
        .expectSuccessfulHandlerExecution()
        .expectEventsMatching(exactSequenceOf(
            messageWithPayload(instanceOf(BidPlacedEvent.class)),
            messageWithPayload(instanceOf(BidRejectedEvent.class))))
        .expectState(auction -> {
          assertEquals(10, auction.getAllBids().size());
          // The new low bidder should NOT be in the list
          boolean newBidderPresent = auction.getAllBids().stream()
              .anyMatch(bid -> bid.bidderId().equals(bidderIds[10]));
          assertFalse(newBidderPresent, "Low bidder should not be in top 10");
        });
  }

  @Test
  void placeBid_bidsAreSortedByAmountDescending() {
    UUID auctionItemId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    UUID bidderId1 = UUID.randomUUID();
    UUID bidderId2 = UUID.randomUUID();
    UUID bidderId3 = UUID.randomUUID();
    String title = "Test Auction Item";
    String description = "A description";
    BigDecimal startingPrice = new BigDecimal("100.00");
    Instant auctionEndTime = FIXED_TIME.plus(Duration.ofDays(7));

    fixture.given(
            new AuctionItemAddedEvent(auctionItemId, sellerId, title, description, startingPrice, auctionEndTime),
            // Place bids in non-sorted order
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderId1, new BigDecimal("150.00")),
            new HighestBidSetEvent(auctionItemId, bidderId1, new BigDecimal("150.00")),
            new BidPlacedEvent(auctionItemId, UUID.randomUUID(), bidderId2, new BigDecimal("175.00")),
            new HighestBidSetEvent(auctionItemId, bidderId2, new BigDecimal("175.00")))
        .when(new PlaceBidCommand(auctionItemId, UUID.randomUUID(), bidderId3, new BigDecimal("200.00")))
        .expectSuccessfulHandlerExecution()
        .expectState(auction -> {
          assertEquals(3, auction.getAllBids().size());
          // Bids should be sorted by amount descending: 200, 175, 150
          assertEquals(new BigDecimal("200.00"), auction.getAllBids().get(0).bidAmount());
          assertEquals(new BigDecimal("175.00"), auction.getAllBids().get(1).bidAmount());
          assertEquals(new BigDecimal("150.00"), auction.getAllBids().get(2).bidAmount());
        });
  }
}

