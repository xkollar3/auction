package edu.fi.muni.cz.marketplace.auction_item.query;

import java.util.UUID;

import lombok.Value;

@Value
public class FindAuctionItemImagesQuery {

    UUID auctionItemId;
}
