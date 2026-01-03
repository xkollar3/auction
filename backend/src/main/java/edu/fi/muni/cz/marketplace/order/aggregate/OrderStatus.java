package edu.fi.muni.cz.marketplace.order.aggregate;

public enum OrderStatus {
  // funds in customer account are reserved
  FUNDS_RESERVED,
  // customer has entered the tracking number
  TRACKING_NUMBER_PROVIDED,
  // order is being tracked and listening for tracking updates
  TRACKING_IN_PROGRESS,
  // order has been delivered to the customer
  DELIVERED,
  // refund is scheduled and will eventually happen
  REFUND_PENDING,
  // customer is refunded the order is cancelled
  CANCELLED
}
