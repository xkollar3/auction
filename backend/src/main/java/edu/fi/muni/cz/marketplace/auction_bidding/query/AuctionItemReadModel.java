package edu.fi.muni.cz.marketplace.auction_bidding.query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.fi.muni.cz.marketplace.auction_bidding.aggregate.AuctionItemCategory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auction_item_read_model")
@Data
@NoArgsConstructor
public class AuctionItemReadModel {

  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID sellerId;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "text")
  private String description;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal startingPrice;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AuctionItemCategory category;

  @Column(nullable = false)
  private Instant auctionEndTime;

  @Column(nullable = false)
  private BigDecimal currentPrice;

  @Column(nullable = false)
  private int bidCount;

  @Column
  private Instant lastBidTime;

  @OneToMany(mappedBy = "auctionItem", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("placedAt DESC")
  private List<BidReadModel> bids = new ArrayList<>();
}
