package edu.fi.muni.cz.marketplace.auction_bidding.event;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Value;

@Value
public class BidPlacedEvent {

  UUID auctionItemId;
  UUID bidId;
  UUID bidderId;
  BigDecimal bidAmount;
}
