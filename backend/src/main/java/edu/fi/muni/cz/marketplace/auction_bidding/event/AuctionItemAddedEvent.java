package edu.fi.muni.cz.marketplace.auction_bidding.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AuctionItemAddedEvent(
    UUID auctionItemId,
    UUID sellerId,
    String title,
    String description,
    BigDecimal startingPrice,
    Instant auctionEndTime
) {

}
