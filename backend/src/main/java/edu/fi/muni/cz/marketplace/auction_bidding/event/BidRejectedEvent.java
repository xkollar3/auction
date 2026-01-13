package edu.fi.muni.cz.marketplace.auction_bidding.event;

import java.util.UUID;
import lombok.Value;

@Value
public class BidRejectedEvent {

  UUID auctionItemId;
  UUID bidderId;
  String reason;
}
