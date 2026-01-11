package edu.fi.muni.cz.marketplace.auction_bidding.aggregate;

/**
 * Represents the state of an auction item.
 */
public enum AuctionStatus {
  /**
   * Auction is currently active and accepting bids.
   */
  ACTIVE,

  /**
   * Auction has been closed, no more bids can be placed.
   */
  CLOSED
}

