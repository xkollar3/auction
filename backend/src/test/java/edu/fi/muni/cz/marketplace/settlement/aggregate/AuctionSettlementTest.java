package edu.fi.muni.cz.marketplace.settlement.aggregate;

import edu.fi.muni.cz.marketplace.settlement.command.ConfirmPurchaseCommand;
import edu.fi.muni.cz.marketplace.settlement.command.RejectPurchaseCommand;
import edu.fi.muni.cz.marketplace.settlement.command.SelectBuyerCommand;
import edu.fi.muni.cz.marketplace.settlement.command.SelectBackupBuyerCommand;
import edu.fi.muni.cz.marketplace.settlement.events.AuctionMarkedUnsuccessfulEvent;
import edu.fi.muni.cz.marketplace.settlement.events.BackupBuyerCandidateSelectedEvent;
import edu.fi.muni.cz.marketplace.settlement.events.BuyerSelectedEvent;
import edu.fi.muni.cz.marketplace.settlement.events.PurchaseRejectedEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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

    List<PotentialBuyer> allBids = List.of(bid1, bid2);

    fixture.givenNoPriorActivity()
        .when(new SelectBuyerCommand(
            settlementId,
            auctionItemId,
            allBids,
            sellerId,
            title))
        .expectSuccessfulHandlerExecution()
        .expectEvents(new BuyerSelectedEvent(
            settlementId,
            auctionItemId,
            allBids,
            0,
            sellerId,
            title))
        .expectState(settlement -> {
          assertEquals(settlementId, settlement.getSettlementId());
          assertEquals(auctionItemId, settlement.getAuctionItemId());
          assertEquals(SettlementStatus.BUYER_SELECTED, settlement.getStatus());
          assertEquals(sellerId, settlement.getSellerId());
          assertEquals(title, settlement.getTitle());

          assertNotNull(settlement.getCurrentBuyer());
          assertEquals(bidder1, settlement.getCurrentBuyer().getBidderId());
          assertEquals(new BigDecimal("100.00"), settlement.getCurrentBuyer().getBidAmount());
          assertEquals(0, settlement.getCurrentBuyerIndex());
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
            sellerId,
            title))
        .expectSuccessfulHandlerExecution()
        .expectEvents(new AuctionMarkedUnsuccessfulEvent(settlementId, auctionItemId))
        .expectState(settlement -> assertEquals(SettlementStatus.UNSUCCESSFUL, settlement.getStatus()));
  }

  @Test
  void testSelectNextBuyer_EmitsBackupCandidateEvent() {
    UUID settlementId = UUID.randomUUID();
    UUID auctionItemId = UUID.randomUUID();
    UUID bidder1 = UUID.randomUUID();
    UUID bidder2 = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    String title = "Auction Item Title";

    PotentialBuyer firstWinner = new PotentialBuyer(bidder1, new BigDecimal("200.00"));
    PotentialBuyer backupBuyer = new PotentialBuyer(bidder2, new BigDecimal("180.00"));

    List<PotentialBuyer> allBids = List.of(firstWinner, backupBuyer);

    fixture.given(new BuyerSelectedEvent(
        settlementId,
        auctionItemId,
        allBids,
        0,
        sellerId,
        title))
        .when(new SelectBackupBuyerCommand(settlementId))
        .expectSuccessfulHandlerExecution()
        .expectEvents(new BackupBuyerCandidateSelectedEvent(settlementId, backupBuyer))
        .expectState(settlement -> {
          assertEquals(SettlementStatus.AWAITING_BACKUP_CONFIRMATION, settlement.getStatus());
          assertEquals(backupBuyer, settlement.getCurrentBuyer());
          assertEquals(1, settlement.getCurrentBuyerIndex());
        });
  }

  @Test
  void testConfirmPurchase_BackupBuyerConfirms_EmitsBuyerSelectedEvent() {
    UUID settlementId = UUID.randomUUID();
    UUID auctionItemId = UUID.randomUUID();
    UUID bidder1 = UUID.randomUUID();
    UUID bidder2 = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    String title = "Auction Item Title";

    PotentialBuyer firstWinner = new PotentialBuyer(bidder1, new BigDecimal("200.00"));
    PotentialBuyer backupBuyer = new PotentialBuyer(bidder2, new BigDecimal("180.00"));

    List<PotentialBuyer> allBids = List.of(firstWinner, backupBuyer);

    fixture.given(
        new BuyerSelectedEvent(settlementId, auctionItemId, allBids, 0, sellerId, title),
        new BackupBuyerCandidateSelectedEvent(settlementId, backupBuyer))
        .when(new ConfirmPurchaseCommand(settlementId))
        .expectSuccessfulHandlerExecution()
        .expectEvents(new BuyerSelectedEvent(
            settlementId,
            auctionItemId,
            allBids,
            1,
            sellerId,
            title))
        .expectState(settlement -> {
          assertEquals(SettlementStatus.BUYER_SELECTED, settlement.getStatus());
          assertEquals(backupBuyer, settlement.getCurrentBuyer());
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

    PotentialBuyer firstWinner = new PotentialBuyer(bidder1, new BigDecimal("150.00"));
    PotentialBuyer backupBuyer = new PotentialBuyer(bidder2, new BigDecimal("140.00"));

    List<PotentialBuyer> allBids = List.of(firstWinner, backupBuyer);

    fixture.given(
        new BuyerSelectedEvent(settlementId, auctionItemId, allBids, 0, sellerId, title),
        new BackupBuyerCandidateSelectedEvent(settlementId, backupBuyer))
        .when(new RejectPurchaseCommand(settlementId))
        .expectSuccessfulHandlerExecution()
        .expectEvents(new PurchaseRejectedEvent(settlementId))
        .expectState(settlement -> {
          assertEquals(SettlementStatus.PENDING, settlement.getStatus());
        });
  }

  @Test
  void testSelectNextBuyer_NoMoreBuyers_MarkUnsuccessful() {
    UUID settlementId = UUID.randomUUID();
    UUID auctionItemId = UUID.randomUUID();
    UUID bidder = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    String title = "Auction Item Title";

    PotentialBuyer onlyBuyer = new PotentialBuyer(bidder, new BigDecimal("50.00"));
    List<PotentialBuyer> allBids = List.of(onlyBuyer);

    fixture.given(new BuyerSelectedEvent(
        settlementId,
        auctionItemId,
        allBids,
        0,
        sellerId,
        title))
        .when(new SelectBackupBuyerCommand(settlementId))
        .expectSuccessfulHandlerExecution()
        .expectEvents(new AuctionMarkedUnsuccessfulEvent(settlementId, auctionItemId))
        .expectState(settlement -> assertEquals(SettlementStatus.UNSUCCESSFUL, settlement.getStatus()));
  }

  @Test
  void testFullFlow_WinnerFailsBackupConfirms_BuyerIsSelected() {
    UUID settlementId = UUID.randomUUID();
    UUID auctionItemId = UUID.randomUUID();
    UUID bidder1 = UUID.randomUUID();
    UUID bidder2 = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    String title = "Auction Item Title";

    PotentialBuyer winner = new PotentialBuyer(bidder1, new BigDecimal("200.00"));
    PotentialBuyer backup = new PotentialBuyer(bidder2, new BigDecimal("180.00"));

    List<PotentialBuyer> allBids = List.of(winner, backup);

    // Simulate: winner selected -> fund reservation fails -> backup selected ->
    // backup confirms
    fixture.given(
        new BuyerSelectedEvent(settlementId, auctionItemId, allBids, 0, sellerId, title),
        new BackupBuyerCandidateSelectedEvent(settlementId, backup))
        .when(new ConfirmPurchaseCommand(settlementId))
        .expectSuccessfulHandlerExecution()
        .expectEvents(new BuyerSelectedEvent(settlementId, auctionItemId, allBids, 1, sellerId, title))
        .expectState(settlement -> {
          assertEquals(SettlementStatus.BUYER_SELECTED, settlement.getStatus());
          assertEquals(backup, settlement.getCurrentBuyer());
          assertEquals(1, settlement.getCurrentBuyerIndex());
        });
  }
}
