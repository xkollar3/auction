package edu.fi.muni.cz.marketplace.auction_bidding.dto;

import java.util.List;

import edu.fi.muni.cz.marketplace.auction_bidding.query.AuctionItemReadModel;
import lombok.Value;

@Value
public class BrowseAuctionItemsResult {
  List<AuctionItemReadModel> items;
  int page;
  int size;
  long totalElements;
  int totalPages;
}
