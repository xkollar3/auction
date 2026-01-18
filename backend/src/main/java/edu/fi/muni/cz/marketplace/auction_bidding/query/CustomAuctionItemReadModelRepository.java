package edu.fi.muni.cz.marketplace.auction_bidding.query;

import org.springframework.data.domain.Page;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionItemCategory;

public interface CustomAuctionItemReadModelRepository {

  Page<AuctionItemReadModel> browse(
      AuctionItemCategory category,
      AuctionSortOption sortOption,
      String searchQuery,
      int page,
      int size);
}
