package edu.fi.muni.cz.marketplace.auction_bidding.event;

import java.math.BigDecimal;
import java.util.UUID;

public record BidPlacedEvent(
    UUID auctionItemId,
    UUID bidderId,
    BigDecimal bidAmount
) {

}
