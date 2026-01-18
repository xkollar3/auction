package edu.fi.muni.cz.marketplace.auction_bidding.query;

import java.util.List;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.auction_bidding.event.ImagesAddedToAuctionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ProcessingGroup("auction_item_images")
@RequiredArgsConstructor
public class AuctionItemImageProjection {

    private final AuctionItemImageReadModelRepository repository;

    @EventHandler
    public void on(ImagesAddedToAuctionEvent event) {
        log.info("Processing ImagesAddedToAuctionEvent for auction item ID: {}", event.getAuctionItemId());

        for (String imageUrl : event.getImageUrls()) {
            AuctionItemImageReadModel image = new AuctionItemImageReadModel(
                    event.getAuctionItemId(),
                    imageUrl);
            repository.save(image);
        }

        log.info("Saved {} images for auction item ID: {}", event.getImageUrls().size(), event.getAuctionItemId());
    }

    @QueryHandler
    public List<AuctionItemImageReadModel> handle(FindAuctionItemImagesQuery query) {
        return repository.findByAuctionItemId(query.getAuctionItemId());
    }
}
