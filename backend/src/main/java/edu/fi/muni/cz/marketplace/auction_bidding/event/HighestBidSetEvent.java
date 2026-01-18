package edu.fi.muni.cz.marketplace.auction_bidding.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Value;

@Value
public class HighestBidSetEvent {

  UUID auctionItemId;
  UUID bidId;
  UUID bidderId;
  BigDecimal bidAmount;
  Instant placedAt;
}
