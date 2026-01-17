package edu.fi.muni.cz.marketplace.settlement.aggregate;

import edu.fi.muni.cz.marketplace.settlement.command.ConfirmPurchaseCommand;
import edu.fi.muni.cz.marketplace.settlement.command.RejectPurchaseCommand;
import edu.fi.muni.cz.marketplace.settlement.command.SelectBuyerCommand;
import edu.fi.muni.cz.marketplace.settlement.command.SelectNextBuyerCommand;
import edu.fi.muni.cz.marketplace.settlement.events.AuctionMarkedUnsuccessfulEvent;
import edu.fi.muni.cz.marketplace.settlement.events.BuyerSelectedEvent;
import edu.fi.muni.cz.marketplace.settlement.events.NextBuyerSelectedEvent;
import edu.fi.muni.cz.marketplace.settlement.events.PurchaseConfirmedEvent;
import edu.fi.muni.cz.marketplace.settlement.events.PurchaseRejectedEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuctionSettlementTest {

    private FixtureConfiguration<AuctionSettlement> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(AuctionSettlement.class);
    }

    @Test
    void testSelectBuyer_Success() {
        UUID settlementId = UUID.randomUUID();
        UUID auctionItemId = UUID.randomUUID();
        UUID bidder1 = UUID.randomUUID();
        UUID bidder2 = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        String title = "Auction Item Title";

        PotentialBuyer bid1 = new PotentialBuyer(bidder1, new BigDecimal("100.00"));
        PotentialBuyer bid2 = new PotentialBuyer(bidder2, new BigDecimal("90.00"));

        List<PotentialBuyer> initialBids = new ArrayList<>();
        initialBids.add(bid1);
        initialBids.add(bid2);

        List<PotentialBuyer> expectedRemaining = new ArrayList<>();
        expectedRemaining.add(bid2);

        fixture.givenNoPriorActivity()
                .when(new SelectBuyerCommand(
                        settlementId,
                        auctionItemId,
                        initialBids,
                        List.of(bidder1, bidder2),
                        sellerId,
                        title
                ))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new BuyerSelectedEvent(
                        settlementId,
                        auctionItemId,
                        bid1,
                        expectedRemaining,
                        sellerId,
                        title
                ))
                .expectState(settlement -> {
                    assertEquals(settlementId, settlement.getSettlementId());
                    assertEquals(auctionItemId, settlement.getAuctionItemId());
                    assertEquals(SettlementStatus.BUYER_SELECTED, settlement.getStatus());
                    assertEquals(sellerId, settlement.getSellerId());
                    assertEquals(title, settlement.getTitle());

                    assertNotNull(settlement.getCurrentBuyer());
                    assertEquals(bidder1, settlement.getCurrentBuyer().bidderId());
                    assertEquals(new BigDecimal("100.00"), settlement.getCurrentBuyer().bidAmount());

                    assertNotNull(settlement.getPotentialBuyerList());
                    assertEquals(1, settlement.getPotentialBuyerList().size());
                    assertEquals(bidder2, settlement.getPotentialBuyerList().getFirst().bidderId());
                });
    }

    @Test
    void testSelectBuyer_NoBids_MarkUnsuccessful() {
        UUID settlementId = UUID.randomUUID();
        UUID auctionItemId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        String title = "Auction Item Title";

        fixture.givenNoPriorActivity()
                .when(new SelectBuyerCommand(
                        settlementId,
                        auctionItemId,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        sellerId,
                        title
                ))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new AuctionMarkedUnsuccessfulEvent(settlementId, auctionItemId))
                .expectState(settlement -> assertEquals(SettlementStatus.UNSUCCESSFUL, settlement.getStatus()));
    }

    @Test
    void testConfirmPurchase() {
        UUID settlementId = UUID.randomUUID();
        UUID auctionItemId = UUID.randomUUID();
        UUID bidder1 = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        String title = "Auction Item Title";

        PotentialBuyer winningBid = new PotentialBuyer(bidder1, new BigDecimal("120.00"));
        List<PotentialBuyer> remaining = Collections.emptyList();

        fixture.given(new BuyerSelectedEvent(
                        settlementId,
                        auctionItemId,
                        winningBid,
                        remaining,
                        sellerId,
                        title
                ))
                .when(new ConfirmPurchaseCommand(settlementId))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new PurchaseConfirmedEvent(settlementId, winningBid, sellerId))
                .expectState(settlement -> {
                    assertEquals(SettlementStatus.BUYER_SELECTED, settlement.getStatus());
                    assertEquals(winningBid, settlement.getCurrentBuyer());
                });
    }

    @Test
    void testRejectPurchase_EmitsPurchaseRejectedEvent() {
        UUID settlementId = UUID.randomUUID();
        UUID auctionItemId = UUID.randomUUID();
        UUID bidder1 = UUID.randomUUID();
        UUID bidder2 = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        String title = "Auction Item Title";

        PotentialBuyer winningBid = new PotentialBuyer(bidder1, new BigDecimal("150.00"));
        PotentialBuyer nextBid = new PotentialBuyer(bidder2, new BigDecimal("140.00"));

        List<PotentialBuyer> remaining = new ArrayList<>();
        remaining.add(nextBid);

        fixture.given(new BuyerSelectedEvent(
                        settlementId,
                        auctionItemId,
                        winningBid,
                        remaining,
                        sellerId,
                        title
                ))
                .when(new RejectPurchaseCommand(settlementId))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new PurchaseRejectedEvent(settlementId))
                .expectState(settlement -> {
                    assertEquals(SettlementStatus.PENDING, settlement.getStatus());
                    assertNull(settlement.getCurrentBuyer());
                });
    }

    @Test
    void testSelectNextBuyer_Success() {
        UUID settlementId = UUID.randomUUID();
        UUID auctionItemId = UUID.randomUUID();
        UUID bidder1 = UUID.randomUUID();
        UUID bidder2 = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        String title = "Auction Item Title";

        PotentialBuyer firstWinner = new PotentialBuyer(bidder1, new BigDecimal("200.00"));
        PotentialBuyer nextWinner = new PotentialBuyer(bidder2, new BigDecimal("180.00"));

        List<PotentialBuyer> remainingAfterFirst = new ArrayList<>();
        remainingAfterFirst.add(nextWinner);

        fixture.given(new BuyerSelectedEvent(
                        settlementId,
                        auctionItemId,
                        firstWinner,
                        remainingAfterFirst,
                        sellerId,
                        title
                ))
                .when(new SelectNextBuyerCommand(settlementId))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new NextBuyerSelectedEvent(settlementId, nextWinner))
                .expectState(settlement -> {
                    assertEquals(SettlementStatus.BUYER_SELECTED, settlement.getStatus());
                    assertEquals(nextWinner, settlement.getCurrentBuyer());
                    assertNotNull(settlement.getPotentialBuyerList());
                    assertTrue(settlement.getPotentialBuyerList().isEmpty());
                });
    }

    @Test
    void testSelectNextBuyer_EmptyAggregateList_MarkUnsuccessful() {
        UUID settlementId = UUID.randomUUID();
        UUID auctionItemId = UUID.randomUUID();

        UUID bidder = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        String title = "Auction Item Title";
        PotentialBuyer winningBid = new PotentialBuyer(bidder, new BigDecimal("50.00"));

        fixture.given(new BuyerSelectedEvent(
                        settlementId,
                        auctionItemId,
                        winningBid,
                        Collections.emptyList(),
                        sellerId,
                        title
                ))
                .when(new SelectNextBuyerCommand(settlementId))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new AuctionMarkedUnsuccessfulEvent(settlementId, auctionItemId))
                .expectState(settlement -> assertEquals(SettlementStatus.UNSUCCESSFUL, settlement.getStatus()));
    }
}
