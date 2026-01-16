package edu.fi.muni.cz.marketplace.auction_bidding.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionItemCategory;
import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionStatus;
import edu.fi.muni.cz.marketplace.auction_bidding.query.AuctionItemReadModel;

public record AuctionItemDetailResponse(
    UUID id,
    UUID sellerId,
    String sellerFirstName,
    String sellerLastName,
    String title,
    String description,
    BigDecimal startingPrice,
    BigDecimal currentPrice,
    int bidCount,
    AuctionItemCategory category,
    AuctionStatus status,
    Instant auctionEndTime,
    List<BidResponse> recentBids) {

  public static AuctionItemDetailResponse from(AuctionItemReadModel readModel) {
    List<BidResponse> recentBids = readModel.getBids().stream()
        .limit(5)
        .map(BidResponse::from)
        .toList();

    return new AuctionItemDetailResponse(
        readModel.getId(),
        readModel.getKeycloakSellerId(),
        readModel.getSellerFirstName(),
        readModel.getSellerLastName(),
        readModel.getTitle(),
        readModel.getDescription(),
        readModel.getStartingPrice(),
        readModel.getCurrentPrice(),
        readModel.getBidCount(),
        readModel.getCategory(),
        readModel.getStatus(),
        readModel.getAuctionEndTime(),
        recentBids);
  }
}
