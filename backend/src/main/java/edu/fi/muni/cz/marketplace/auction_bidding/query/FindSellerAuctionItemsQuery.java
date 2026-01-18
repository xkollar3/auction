package edu.fi.muni.cz.marketplace.auction_bidding.query;

import java.util.UUID;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionStatus;
import lombok.Value;

@Value
public class FindSellerAuctionItemsQuery {
  UUID keycloakSellerId;
  AuctionStatus status;
}
