package edu.fi.muni.cz.marketplace.auction_bidding.query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BidReadModelRepository extends JpaRepository<BidReadModel, UUID> {

  List<BidReadModel> findTop5ByAuctionItemIdOrderByPlacedAtDesc(UUID auctionItemId);

  @Query("SELECT COUNT(b) FROM BidReadModel b WHERE b.auctionItem.id = :auctionItemId AND b.placedAt >= :since")
  long countRecentBids(@Param("auctionItemId") UUID auctionItemId, @Param("since") Instant since);

  @Query("SELECT DISTINCT b.auctionItem FROM BidReadModel b WHERE b.bidderId = :bidderId AND b.auctionItem.status = :status ORDER BY b.auctionItem.auctionEndTime ASC")
  List<AuctionItemReadModel> findAuctionItemsByBidderIdAndStatus(@Param("bidderId") UUID bidderId, @Param("status") edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionStatus status);
}
