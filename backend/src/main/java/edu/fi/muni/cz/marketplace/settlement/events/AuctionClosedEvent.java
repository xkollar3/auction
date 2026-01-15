package edu.fi.muni.cz.marketplace.settlement.events;

import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class AuctionClosedEvent {

    UUID auctionId;
    List<UUID> topBuyerIds;

}
