package edu.fi.muni.cz.marketplace.settlement.command;

import edu.fi.muni.cz.marketplace.settlement.aggregate.BidSettlement;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;
import java.util.UUID;

@Value
public class SelectNextBuyerCommand {

    @TargetAggregateIdentifier
    UUID settlementId;
    List<BidSettlement> bidSettlementList;

}
