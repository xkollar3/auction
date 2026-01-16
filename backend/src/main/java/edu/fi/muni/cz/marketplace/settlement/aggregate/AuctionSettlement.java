package edu.fi.muni.cz.marketplace.settlement.aggregate;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.Bid;
import edu.fi.muni.cz.marketplace.order.command.ReserveFundsCommand;
import edu.fi.muni.cz.marketplace.settlement.command.ConfirmPurchaseCommand;
import edu.fi.muni.cz.marketplace.settlement.command.MarkAuctionUnsuccessfulCommand;
import edu.fi.muni.cz.marketplace.settlement.command.RejectPurchaseCommand;
import edu.fi.muni.cz.marketplace.settlement.command.SelectBuyerCommand;
import edu.fi.muni.cz.marketplace.settlement.command.SelectNextBuyerCommand;
import edu.fi.muni.cz.marketplace.settlement.events.AuctionMarkedUnsuccessfulEvent;
import edu.fi.muni.cz.marketplace.settlement.events.BuyerSelectedEvent;
import edu.fi.muni.cz.marketplace.settlement.events.NextBuyerSelectedEvent;
import edu.fi.muni.cz.marketplace.settlement.events.PurchaseConfirmedEvent;
import edu.fi.muni.cz.marketplace.settlement.events.PurchaseRejectedEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Slf4j
@Getter
@Aggregate
@NoArgsConstructor
public class AuctionSettlement {

    @AggregateIdentifier
    private UUID settlementId;
    private UUID auctionItemId;
    private SettlementStatus status;
    private UUID sellerId;
    private String title;

    private List<BidSettlement> bidSettlementList;
    private BidSettlement winningBid;

    @CommandHandler
    public AuctionSettlement(SelectBuyerCommand command) {
        log.debug("Handling SelectBuyerCommand: settlementId=" + command.getSettlementId());
        List<BidSettlement> bidSettlements = command.getBidSettlementList();
        if (Objects.isNull(bidSettlements) || bidSettlements.isEmpty()) {
            apply(new AuctionMarkedUnsuccessfulEvent(command.getSettlementId(), command.getAuctionItemId()));
            return;
        }
        BidSettlement selectedWinningBid = command.getBidSettlementList().getFirst();
        command.getBidSettlementList().removeFirst();
        apply(new BuyerSelectedEvent(command.getSettlementId(), command.getAuctionItemId(), selectedWinningBid, command.getBidSettlementList(), command.getSellerId(), command.getTitle()));
    }

    @EventSourcingHandler
    public void on(BuyerSelectedEvent event) {
        this.settlementId = event.getSettlementId();
        this.auctionItemId = event.getAuctionItemId();
        this.status = SettlementStatus.BUYER_SELECTED;
        this.sellerId = event.getSellerId();
        this.title = event.getTitle();
        this.bidSettlementList = event.getBidSettlementList();
        this.winningBid = event.getWinningBid();
    }

    @CommandHandler
    public void handle(SelectNextBuyerCommand command) {
        if (Objects.isNull(bidSettlementList) || bidSettlementList.isEmpty()) {
            apply(new AuctionMarkedUnsuccessfulEvent(command.getSettlementId(), auctionItemId));
            return;
        }
        BidSettlement selectedWinningBid = command.getBidSettlementList().getFirst();
        command.getBidSettlementList().removeFirst();
        apply(new NextBuyerSelectedEvent(command.getSettlementId(), selectedWinningBid, command.getBidSettlementList()));
    }

    @EventSourcingHandler
    public void on(NextBuyerSelectedEvent event) {
        this.settlementId = event.getSettlementId();
        this.winningBid = event.getWinningBid();
        this.bidSettlementList = new ArrayList<>(event.getBidSettlementList());
        this.status = SettlementStatus.BUYER_SELECTED;
    }

    @CommandHandler
    public void handle(ConfirmPurchaseCommand command) {
        apply(new PurchaseConfirmedEvent(settlementId, winningBid, sellerId));
    }

    @EventSourcingHandler
    public void on(PurchaseConfirmedEvent event) {
        this.status = SettlementStatus.BUYER_SELECTED;
    }

    @CommandHandler
    public void handle(RejectPurchaseCommand command) {
        apply(new PurchaseRejectedEvent(command.getSettlementId(), bidSettlementList));
    }

    @EventSourcingHandler
    public void on(PurchaseRejectedEvent event) {
        this.settlementId = event.getSettlementId();
        this.winningBid = null;
        this.status = SettlementStatus.PENDING;
    }

    @CommandHandler
    public void handle(MarkAuctionUnsuccessfulCommand command) {
        apply(new AuctionMarkedUnsuccessfulEvent(command.getSettlementId(), command.getAuctionItemId()));
    }

    @EventSourcingHandler
    public void on(AuctionMarkedUnsuccessfulEvent event) {
        this.settlementId = event.getSettlementId();
        this.auctionItemId = event.getAuctionItemId();
        this.winningBid = null;
        this.status = SettlementStatus.UNSUCCESSFUL;
    }

}