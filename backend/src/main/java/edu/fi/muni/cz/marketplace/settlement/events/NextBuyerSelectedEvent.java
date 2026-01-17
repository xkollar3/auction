package edu.fi.muni.cz.marketplace.settlement.events;

import edu.fi.muni.cz.marketplace.settlement.aggregate.PotentialBuyer;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class NextBuyerSelectedEvent {

    UUID settlementId;
    PotentialBuyer selectedPotentialBuyer;

}
