package edu.fi.muni.cz.marketplace.auction_bidding.query;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionItemCategory;

@Repository
public interface AuctionItemReadModelRepository extends JpaRepository<AuctionItemReadModel, UUID>, CustomAuctionItemReadModelRepository {

  List<AuctionItemReadModel> findBySellerId(UUID sellerId);

  List<AuctionItemReadModel> findByCategory(AuctionItemCategory category);

  @Query(value = """
      SELECT a.*
      FROM auction_item_read_model a
      WHERE a.search_vector @@ websearch_to_tsquery('english', :query)
      ORDER BY ts_rank(a.search_vector, websearch_to_tsquery('english', :query)) DESC
      LIMIT :limit
      """, nativeQuery = true)
  List<AuctionItemReadModel> searchByText(@Param("query") String query, @Param("limit") int limit);
}
