package edu.fi.muni.cz.marketplace.auction_bidding.query;

import lombok.Value;

@Value
public class SearchAuctionItemsQuery {
  String query;
  int limit;
}
