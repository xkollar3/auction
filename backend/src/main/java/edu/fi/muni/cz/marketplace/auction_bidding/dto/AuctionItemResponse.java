package edu.fi.muni.cz.marketplace.auction_bidding.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionItemCategory;
import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionStatus;
import edu.fi.muni.cz.marketplace.auction_bidding.query.AuctionItemReadModel;

public record AuctionItemResponse(
    UUID id,
    UUID sellerId,
    String title,
    String description,
    BigDecimal startingPrice,
    BigDecimal currentPrice,
    int bidCount,
    AuctionItemCategory category,
    AuctionStatus status,
    Instant auctionEndTime,
    String imageUrl) {

  public static AuctionItemResponse from(AuctionItemReadModel readModel) {
    return new AuctionItemResponse(
        readModel.getId(),
        readModel.getKeycloakSellerId(),
        readModel.getTitle(),
        readModel.getDescription(),
        readModel.getStartingPrice(),
        readModel.getCurrentPrice(),
        readModel.getBidCount(),
        readModel.getCategory(),
        readModel.getStatus(),
        readModel.getAuctionEndTime(),
        readModel.getImageUrl());
  }
}
