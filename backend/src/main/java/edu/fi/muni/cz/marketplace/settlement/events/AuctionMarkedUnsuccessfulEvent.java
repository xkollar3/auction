package edu.fi.muni.cz.marketplace.settlement.events;

import lombok.Value;

import java.util.UUID;

@Value
public class AuctionMarkedUnsuccessfulEvent {

    UUID settlementId;
    UUID auctionItemId;

}
