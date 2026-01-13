package edu.fi.muni.cz.marketplace.auction_bidding.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AddAuctionItemRequest(
    Instant auctionEndTime,
    BigDecimal startingPrice,
    String description,
    String title
) {

}



