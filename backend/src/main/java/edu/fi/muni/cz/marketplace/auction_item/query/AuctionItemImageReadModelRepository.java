package edu.fi.muni.cz.marketplace.auction_item.query;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionItemImageReadModelRepository extends JpaRepository<AuctionItemImageReadModel, UUID> {

    List<AuctionItemImageReadModel> findByAuctionItemId(UUID auctionItemId);
}
