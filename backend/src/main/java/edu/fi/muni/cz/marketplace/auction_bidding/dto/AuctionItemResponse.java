package edu.fi.muni.cz.marketplace.auction_bidding.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionItemCategory;
import edu.fi.muni.cz.marketplace.auction_bidding.query.AuctionItemReadModel;

public record AuctionItemResponse(
    UUID id,
    UUID sellerId,
    String title,
    String description,
    BigDecimal startingPrice,
    AuctionItemCategory category,
    Instant auctionEndTime) {

  public static AuctionItemResponse from(AuctionItemReadModel readModel) {
    return new AuctionItemResponse(
        readModel.getId(),
        readModel.getSellerId(),
        readModel.getTitle(),
        readModel.getDescription(),
        readModel.getStartingPrice(),
        readModel.getCategory(),
        readModel.getAuctionEndTime());
  }
}
