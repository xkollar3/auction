package edu.fi.muni.cz.marketplace.settlement.aggregate;

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
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

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
    private List<PotentialBuyer> potentialBuyerList;
    private PotentialBuyer currentBuyer;

    @CommandHandler
    public AuctionSettlement(SelectBuyerCommand command) {
        log.debug("Handling SelectBuyerCommand: settlementId=" + command.getSettlementId());
        List<PotentialBuyer> potentialBuyers = command.getPotentialBuyerList();
        if (Objects.isNull(potentialBuyers) || potentialBuyers.isEmpty()) {
            apply(new AuctionMarkedUnsuccessfulEvent(command.getSettlementId(), command.getAuctionItemId()));
            return;
        }
        List<PotentialBuyer> potentialBuyersList = new ArrayList<>(command.getPotentialBuyerList());
        PotentialBuyer selectedCurrentBuyer = potentialBuyersList.getFirst();
        potentialBuyersList.removeFirst();
        apply(new BuyerSelectedEvent(command.getSettlementId(), command.getAuctionItemId(), selectedCurrentBuyer, potentialBuyersList, command.getSellerId(), command.getTitle()));
    }

    @EventSourcingHandler
    public void on(BuyerSelectedEvent event) {
        this.settlementId = event.getSettlementId();
        this.auctionItemId = event.getAuctionItemId();
        this.status = SettlementStatus.BUYER_SELECTED;
        this.sellerId = event.getSellerId();
        this.title = event.getTitle();
        this.potentialBuyerList = event.getPotentialBuyerList();
        this.currentBuyer = event.getSelectedPotentialBuyer();
    }

    @CommandHandler
    public void handle(SelectNextBuyerCommand command) {
        if (Objects.isNull(potentialBuyerList) || potentialBuyerList.isEmpty()) {
            apply(new AuctionMarkedUnsuccessfulEvent(command.getSettlementId(), auctionItemId));
            return;
        }
        PotentialBuyer selectedPotentialBuyer = this.getPotentialBuyerList().getFirst();
        apply(new NextBuyerSelectedEvent(command.getSettlementId(), selectedPotentialBuyer));
    }

    @EventSourcingHandler
    public void on(NextBuyerSelectedEvent event) {
        this.settlementId = event.getSettlementId();
        this.currentBuyer = event.getSelectedPotentialBuyer();
        this.potentialBuyerList.remove(event.getSelectedPotentialBuyer());
        this.status = SettlementStatus.BUYER_SELECTED;
    }

    @CommandHandler
    public void handle(ConfirmPurchaseCommand command) {
        apply(new PurchaseConfirmedEvent(settlementId, currentBuyer, sellerId));
    }

    @EventSourcingHandler
    public void on(PurchaseConfirmedEvent event) {
        this.status = SettlementStatus.BUYER_SELECTED;
    }

    @CommandHandler
    public void handle(RejectPurchaseCommand command) {
        apply(new PurchaseRejectedEvent(command.getSettlementId()));
    }

    @EventSourcingHandler
    public void on(PurchaseRejectedEvent event) {
        this.currentBuyer = null;
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
        this.currentBuyer = null;
        this.status = SettlementStatus.UNSUCCESSFUL;
    }

}