package edu.fi.muni.cz.marketplace.auction_bidding.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.Bid;
import lombok.Value;

@Value
public class AuctionClosedEvent {

  UUID auctionItemId;
  UUID sellerId;
  String title;
  List<WinningBid> winningBids;

  @Value
  public static class WinningBid {
    UUID bidId;
    UUID bidderId;
    BigDecimal bidAmount;

    public WinningBid(Bid bid) {
      this.bidId = bid.bidId();
      this.bidderId = bid.bidderId();
      this.bidAmount = bid.bidAmount();
    }
  }
}
