package edu.fi.muni.cz.marketplace.order.aggregate;

public enum OrderStatus {
  // funds in customer account are reserved
  FUNDS_RESERVED,
  // tracking number submitted, waiting for Ship24 registration
  TRACKING_PENDING,
  // order is being tracked and listening for tracking updates
  TRACKING_IN_PROGRESS,
  // order has been delivered to the customer
  DELIVERED,
  // refund is scheduled and will eventually happen
  REFUND_PENDING,
  // customer is refunded the order is cancelled
  CANCELLED,
  // order is completed
  COMPLETED
}
