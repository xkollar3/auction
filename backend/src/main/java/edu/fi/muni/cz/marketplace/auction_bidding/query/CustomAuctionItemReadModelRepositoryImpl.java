package edu.fi.muni.cz.marketplace.auction_bidding.query;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionItemCategory;
import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
    boolean isHotSort = sortOption == AuctionSortOption.HOT;

    StringBuilder queryBuilder = new StringBuilder();
    StringBuilder countBuilder = new StringBuilder();

    if (isHotSort) {
      queryBuilder.append("""
          SELECT a FROM AuctionItemReadModel a
          LEFT JOIN a.bids b ON b.placedAt >= :oneHourAgo
          """);
      countBuilder.append("""
          SELECT COUNT(DISTINCT a) FROM AuctionItemReadModel a
          LEFT JOIN a.bids b ON b.placedAt >= :oneHourAgo
          """);
    } else {
      queryBuilder.append("SELECT a FROM AuctionItemReadModel a ");
      countBuilder.append("SELECT COUNT(a) FROM AuctionItemReadModel a ");
    }

    List<String> conditions = new ArrayList<>();
    conditions.add("a.status = :status");

    if (hasCategory) {
      conditions.add("a.category = :category");
    }

    if (hasSearch) {
      conditions.add("(LOWER(a.title) LIKE LOWER(:searchPattern) OR LOWER(a.description) LIKE LOWER(:searchPattern))");
    }

    String whereClause = "WHERE " + String.join(" AND ", conditions) + " ";
    queryBuilder.append(whereClause);
    countBuilder.append(whereClause);

    if (isHotSort) {
      queryBuilder.append("GROUP BY a ");
    }

    switch (sortOption) {
      case ENDING_SOON -> queryBuilder.append("ORDER BY a.auctionEndTime ASC");
      case HOT -> queryBuilder.append("ORDER BY COUNT(b) DESC, a.auctionEndTime ASC");
      case PRICE_HIGH_TO_LOW -> queryBuilder.append("ORDER BY a.currentPrice DESC");
      case PRICE_LOW_TO_HIGH -> queryBuilder.append("ORDER BY a.currentPrice ASC");
    }

    TypedQuery<AuctionItemReadModel> query = entityManager.createQuery(queryBuilder.toString(), AuctionItemReadModel.class);
    TypedQuery<Long> countQuery = entityManager.createQuery(countBuilder.toString(), Long.class);

    query.setParameter("status", AuctionStatus.ACTIVE);
    countQuery.setParameter("status", AuctionStatus.ACTIVE);

    if (isHotSort) {
      Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
      query.setParameter("oneHourAgo", oneHourAgo);
      countQuery.setParameter("oneHourAgo", oneHourAgo);
    }

    if (hasCategory) {
      query.setParameter("category", category);
      countQuery.setParameter("category", category);
    }

    if (hasSearch) {
      String searchPattern = "%" + searchQuery + "%";
      query.setParameter("searchPattern", searchPattern);
      countQuery.setParameter("searchPattern", searchPattern);
    }

    query.setFirstResult(page * size);
    query.setMaxResults(size);

    List<AuctionItemReadModel> results = query.getResultList();
    Long total = countQuery.getSingleResult();

    return new PageImpl<>(results, PageRequest.of(page, size), total);
  }
}
