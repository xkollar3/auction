package edu.fi.muni.cz.marketplace.settlement.events;

import edu.fi.muni.cz.marketplace.settlement.aggregate.PotentialBuyer;
import lombok.Value;

import java.util.UUID;

@Value
public class PurchaseConfirmedEvent {

    UUID settlementId;
    PotentialBuyer potentialBuyer;
    UUID sellerId;

}
