package edu.fi.muni.cz.marketplace.auction_bidding.event;

import java.util.UUID;

public record BidRejectedEvent(
    UUID auctionItemId,
    UUID bidderId,
    String reason
) {

}
