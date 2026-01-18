package edu.fi.muni.cz.marketplace.auction_bidding.query;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionItemCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CustomAuctionItemReadModelRepositoryImpl implements CustomAuctionItemReadModelRepository {

  private final EntityManager entityManager;

  @Override
  public Page<AuctionItemReadModel> browse(
      AuctionItemCategory category,
      AuctionSortOption sortOption,
      String searchQuery,
      int page,
      int size) {

    boolean hasCategory = category != null;
    boolean hasSearch = searchQuery != null && !searchQuery.isBlank();

    StringBuilder queryBuilder = new StringBuilder("SELECT * FROM auction_item_read_model a ");
    StringBuilder countBuilder = new StringBuilder("SELECT COUNT(*) FROM auction_item_read_model a ");

    List<String> conditions = new ArrayList<>();
    conditions.add("a.status = 'ACTIVE'");

    if (hasCategory) {
      conditions.add("a.category = :category");
    }

    if (hasSearch) {
      conditions.add("a.search_vector @@ websearch_to_tsquery('english', :searchQuery)");
    }

    String whereClause = "WHERE " + String.join(" AND ", conditions) + " ";
    queryBuilder.append(whereClause);
    countBuilder.append(whereClause);

    String orderBy = switch (sortOption) {
      case ENDING_SOON -> "ORDER BY a.auction_end_time ASC";
      case HOT -> "ORDER BY a.bid_count DESC, a.auction_end_time ASC";
      case PRICE_HIGH_TO_LOW -> "ORDER BY a.current_price DESC";
      case PRICE_LOW_TO_HIGH -> "ORDER BY a.current_price ASC";
    };

    if (hasSearch) {
      queryBuilder.append("ORDER BY ts_rank(a.search_vector, websearch_to_tsquery('english', :searchQuery)) DESC, ");
      queryBuilder.append(orderBy.replace("ORDER BY ", ""));
    } else {
      queryBuilder.append(orderBy);
    }

    Query query = entityManager.createNativeQuery(queryBuilder.toString(), AuctionItemReadModel.class);
    Query countQuery = entityManager.createNativeQuery(countBuilder.toString());

    if (hasCategory) {
      query.setParameter("category", category.name());
      countQuery.setParameter("category", category.name());
    }

    if (hasSearch) {
      query.setParameter("searchQuery", searchQuery);
      countQuery.setParameter("searchQuery", searchQuery);
    }

    query.setFirstResult(page * size);
    query.setMaxResults(size);

    List<AuctionItemReadModel> results = query.getResultList();
    Long total = ((Number) countQuery.getSingleResult()).longValue();

    return new PageImpl<>(results, PageRequest.of(page, size), total);
  }
}
