package edu.fi.muni.cz.marketplace.order.dto;

import edu.fi.muni.cz.marketplace.order.aggregate.OrderStatus;
import edu.fi.muni.cz.marketplace.order.aggregate.TrackingStatusMilestone;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    UUID buyerId,
    UUID sellerId,
    OrderStatus status,
    BigDecimal amount,
    Instant reservedAt,
    String trackingNumber,
    TrackingStatusMilestone trackingStatusMilestone,
    String trackingEventStatus,
    Instant trackingLastUpdatedAt,
    Instant completedAt,
    Instant cancelledAt
) {}
