package edu.fi.muni.cz.marketplace.settlement.command;

import edu.fi.muni.cz.marketplace.settlement.aggregate.BidSettlement;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;
import java.util.UUID;

@Value
public class SelectBuyerCommand {

    @TargetAggregateIdentifier
    UUID settlementId;
    UUID auctionItemId;
    List<BidSettlement> bidSettlementList;
    List<UUID> biddersIdList;
    UUID sellerId;
    String title;

}
