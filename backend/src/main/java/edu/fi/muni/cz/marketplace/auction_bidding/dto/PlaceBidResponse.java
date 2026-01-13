package edu.fi.muni.cz.marketplace.auction_bidding.dto;

import lombok.Value;

/**
 * Result of placing a bid on an auction item.
 */
@Value
public class PlaceBidResponse {

  boolean accepted;
  String reason;

  public static PlaceBidResponse success() {
    return new PlaceBidResponse(true, null);
  }

  public static PlaceBidResponse failure(String reason) {
    return new PlaceBidResponse(false, reason);
  }
}

