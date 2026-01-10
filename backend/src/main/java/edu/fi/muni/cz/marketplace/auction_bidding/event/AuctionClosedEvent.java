package edu.fi.muni.cz.marketplace.auction_bidding.event;

import edu.fi.muni.cz.marketplace.auction_bidding.dto.Bid;
import java.util.List;
import java.util.UUID;

public record AuctionClosedEvent(
    UUID auctionItemId,
    List<Bid> winningBids
) {

}
