package edu.fi.muni.cz.marketplace.auction_bidding.event;

import edu.fi.muni.cz.marketplace.auction_bidding.dto.Bid;
import java.util.List;
import java.util.UUID;
import lombok.Value;

@Value
public class AuctionClosedEvent {

  UUID auctionItemId;
  List<Bid> winningBids;
}
