package edu.fi.muni.cz.marketplace.auction_item.event;

import java.util.List;
import java.util.UUID;

import lombok.Value;

@Value
public class ImagesAddedToAuctionEvent {

    UUID auctionItemId;
    List<String> imageUrls;
}
