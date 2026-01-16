package edu.fi.muni.cz.marketplace.auction_bidding.query;

import java.util.List;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.auction_bidding.event.AuctionItemAddedEvent;
import edu.fi.muni.cz.marketplace.auction_bidding.event.HighestBidSetEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ProcessingGroup("auction_item_read_model")
@RequiredArgsConstructor
public class AuctionItemProjection {

  private final AuctionItemReadModelRepository repository;
  private final BidReadModelRepository bidRepository;

  @EventHandler
  public void on(AuctionItemAddedEvent event) {
    log.info("Processing AuctionItemAddedEvent for auction item ID: {}", event.getAuctionItemId());

    AuctionItemReadModel readModel = new AuctionItemReadModel();
    readModel.setId(event.getAuctionItemId());
    readModel.setSellerId(event.getSellerId());
    readModel.setTitle(event.getTitle());
    readModel.setDescription(event.getDescription());
    readModel.setStartingPrice(event.getStartingPrice());
    readModel.setCategory(event.getCategory());
    readModel.setAuctionEndTime(event.getAuctionEndTime());
    readModel.setCurrentPrice(event.getStartingPrice());
    readModel.setBidCount(0);

    repository.save(readModel);
    log.info("Saved auction item read model for ID: {}", event.getAuctionItemId());
  }

  @EventHandler
  public void on(HighestBidSetEvent event) {
    log.info("Processing HighestBidSetEvent for auction item ID: {}", event.getAuctionItemId());

    AuctionItemReadModel auctionItem = repository.findById(event.getAuctionItemId())
        .orElseThrow(() -> new IllegalStateException("Auction item not found: " + event.getAuctionItemId()));

    BidReadModel bid = new BidReadModel();
    bid.setId(event.getBidId());
    bid.setAuctionItem(auctionItem);
    bid.setBidderId(event.getBidderId());
    bid.setBidAmount(event.getBidAmount());
    bid.setPlacedAt(event.getPlacedAt());
    bidRepository.save(bid);

    auctionItem.setBidCount(auctionItem.getBidCount() + 1);
    auctionItem.setLastBidTime(event.getPlacedAt());
    auctionItem.setCurrentPrice(event.getBidAmount());
    repository.save(auctionItem);

    log.info("Saved accepted bid for auction item ID: {}", event.getAuctionItemId());
  }

  @QueryHandler
  public AuctionItemReadModel handle(FindAuctionItemByIdQuery query) {
    return repository.findById(query.getAuctionItemId()).orElse(null);
  }

  @QueryHandler
  public List<AuctionItemReadModel> handle(SearchAuctionItemsQuery query) {
    return repository.searchByText(query.getQuery(), query.getLimit());
  }

  @QueryHandler
  public Page<AuctionItemReadModel> handle(BrowseAuctionItemsQuery query) {
    return repository.browse(
        query.getCategory(),
        query.getSortOption(),
        query.getSearchQuery(),
        query.getPage(),
        query.getSize());
  }
}
