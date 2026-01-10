package edu.fi.muni.cz.marketplace.auction_bidding.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a bid placed on an auction item. This is used to track all bids for an auction.
 */
public record Bid(
    UUID bidId,
    UUID bidderId,
    BigDecimal bidAmount
) {

}

