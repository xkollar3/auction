package edu.fi.muni.cz.marketplace.settlement.aggregate;

import edu.fi.muni.cz.marketplace.settlement.command.ConfirmPurchaseCommand;
import edu.fi.muni.cz.marketplace.settlement.command.MarkAuctionUnsuccessfulCommand;
import edu.fi.muni.cz.marketplace.settlement.command.RejectPurchaseCommand;
import edu.fi.muni.cz.marketplace.settlement.command.SelectBuyerCommand;
import edu.fi.muni.cz.marketplace.settlement.command.SelectBackupBuyerCommand;
import edu.fi.muni.cz.marketplace.settlement.events.AuctionMarkedUnsuccessfulEvent;
import edu.fi.muni.cz.marketplace.settlement.events.BackupBuyerCandidateSelectedEvent;
import edu.fi.muni.cz.marketplace.settlement.events.BuyerSelectedEvent;
import edu.fi.muni.cz.marketplace.settlement.events.PurchaseRejectedEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

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
    private int currentBuyerIndex;

    @CommandHandler
    public AuctionSettlement(SelectBuyerCommand command) {
        log.debug("Handling SelectBuyerCommand: settlementId=" + command.getSettlementId());
        List<PotentialBuyer> potentialBuyers = command.getPotentialBuyerList();
        if (Objects.isNull(potentialBuyers) || potentialBuyers.isEmpty()) {
            apply(new AuctionMarkedUnsuccessfulEvent(command.getSettlementId(), command.getAuctionItemId()));
            return;
        }
        apply(new BuyerSelectedEvent(
            command.getSettlementId(),
            command.getAuctionItemId(),
            potentialBuyers,
            0,
            command.getSellerId(),
            command.getTitle()));
    }

    @EventSourcingHandler
    public void on(BuyerSelectedEvent event) {
        this.settlementId = event.getSettlementId();
        this.auctionItemId = event.getAuctionItemId();
        this.status = SettlementStatus.BUYER_SELECTED;
        this.sellerId = event.getSellerId();
        this.title = event.getTitle();
        this.potentialBuyerList = event.getPotentialBuyerList();
        this.currentBuyerIndex = event.getCurrentBuyerIndex();
    }

    @CommandHandler
    public void handle(SelectBackupBuyerCommand command) {
        int nextIndex = this.currentBuyerIndex + 1;
        if (nextIndex >= this.potentialBuyerList.size()) {
            apply(new AuctionMarkedUnsuccessfulEvent(command.getSettlementId(), auctionItemId));
            return;
        }
        apply(new BackupBuyerCandidateSelectedEvent(
            this.settlementId,
            this.potentialBuyerList.get(nextIndex)));
    }

    @EventSourcingHandler
    public void on(BackupBuyerCandidateSelectedEvent event) {
        this.currentBuyerIndex++;
        this.status = SettlementStatus.AWAITING_BACKUP_CONFIRMATION;
    }

    @CommandHandler
    public void handle(ConfirmPurchaseCommand command) {
        apply(new BuyerSelectedEvent(
            this.settlementId,
            this.auctionItemId,
            this.potentialBuyerList,
            this.currentBuyerIndex,
            this.sellerId,
            this.title));
    }

    public PotentialBuyer getCurrentBuyer() {
        return potentialBuyerList.get(currentBuyerIndex);
    }

    @CommandHandler
    public void handle(RejectPurchaseCommand command) {
        apply(new PurchaseRejectedEvent(command.getSettlementId()));
    }

    @EventSourcingHandler
    public void on(PurchaseRejectedEvent event) {
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
        this.status = SettlementStatus.UNSUCCESSFUL;
    }

}