package edu.fi.muni.cz.marketplace.order.query;

import edu.fi.muni.cz.marketplace.order.aggregate.OrderStatus;
import edu.fi.muni.cz.marketplace.order.aggregate.TrackingStatusMilestone;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_read_model")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReadModel {

  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID buyerId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  @Column(nullable = false)
  private String paymentIntentId;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false)
  private Instant reservedAt;

  @Column(nullable = false)
  private UUID sellerId;

  @Column(nullable = false)
  private String sellerAccountId;

  private String trackingNumber;

  private String ship24TrackerId;

  private Instant trackingEnteredAt;

  @Enumerated(EnumType.STRING)
  private TrackingStatusMilestone trackingStatusMilestone;

  private String trackingEventStatus;

  private Instant trackingLastUpdatedAt;

  private Instant completedAt;

  private Instant cancelledAt;

  private String refundId;
}
