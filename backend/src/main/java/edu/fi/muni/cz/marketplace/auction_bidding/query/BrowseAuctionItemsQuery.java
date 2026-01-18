package edu.fi.muni.cz.marketplace.auction_bidding.query;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionItemCategory;
import lombok.Value;

@Value
public class BrowseAuctionItemsQuery {
  AuctionItemCategory category; // null means all categories
  AuctionSortOption sortOption;
  String searchQuery; // null means no text search
  int page;
  int size;
}
