package edu.fi.muni.cz.marketplace.settlement.events;

import edu.fi.muni.cz.marketplace.settlement.aggregate.BidSettlement;
import lombok.Value;

import java.util.UUID;

@Value
public class PurchaseConfirmedEvent {

    UUID settlementId;
    BidSettlement winningBid;
    UUID sellerId;

}
