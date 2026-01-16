package edu.fi.muni.cz.marketplace.auction_bidding.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionItemCategory;
import lombok.Value;

@Value
public class AuctionItemAddedEvent {

  UUID auctionItemId;
  UUID sellerId;
  String title;
  String description;
  BigDecimal startingPrice;
  AuctionItemCategory category;
  Instant auctionEndTime;
}
