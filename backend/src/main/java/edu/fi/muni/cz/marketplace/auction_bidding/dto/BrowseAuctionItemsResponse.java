package edu.fi.muni.cz.marketplace.auction_bidding.dto;

import java.util.List;

public record BrowseAuctionItemsResponse(
    List<AuctionItemResponse> items,
    int page,
    int size,
    long totalElements,
    int totalPages) {
}
