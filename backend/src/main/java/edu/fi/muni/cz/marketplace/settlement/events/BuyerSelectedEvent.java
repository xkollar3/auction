package edu.fi.muni.cz.marketplace.settlement.events;

import edu.fi.muni.cz.marketplace.settlement.aggregate.BidSettlement;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Value
public class BuyerSelectedEvent {

    UUID settlementId;
    UUID auctionItemId;
    BidSettlement winningBid;
    List<BidSettlement> bidSettlementList;
    UUID sellerId;
    String title;


}
