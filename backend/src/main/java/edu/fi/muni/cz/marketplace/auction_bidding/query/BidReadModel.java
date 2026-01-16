package edu.fi.muni.cz.marketplace.auction_bidding.query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bid_read_model")
@Data
@NoArgsConstructor
public class BidReadModel {

  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "auction_item_id", nullable = false)
  private AuctionItemReadModel auctionItem;

  @Column(nullable = false)
  private UUID bidderId;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal bidAmount;

  @Column(nullable = false)
  private Instant placedAt;
}
