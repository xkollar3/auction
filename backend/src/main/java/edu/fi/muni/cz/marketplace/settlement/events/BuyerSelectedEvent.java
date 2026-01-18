package edu.fi.muni.cz.marketplace.settlement.events;

import edu.fi.muni.cz.marketplace.settlement.aggregate.PotentialBuyer;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class BuyerSelectedEvent {

    UUID settlementId;
    UUID auctionItemId;
    List<PotentialBuyer> potentialBuyerList;
    int currentBuyerIndex;
    UUID sellerId;
    String title;

    public PotentialBuyer getSelectedPotentialBuyer() {
        return potentialBuyerList.get(currentBuyerIndex);
    }

}
