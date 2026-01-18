package edu.fi.muni.cz.marketplace.auction_bidding.dto;

import java.math.BigDecimal;
import java.time.Instant;

import edu.fi.muni.cz.marketplace.auction_bidding.query.BidReadModel;

public record BidResponse(
    BigDecimal amount,
    Instant placedAt) {

  public static BidResponse from(BidReadModel bid) {
    return new BidResponse(bid.getBidAmount(), bid.getPlacedAt());
  }
}
