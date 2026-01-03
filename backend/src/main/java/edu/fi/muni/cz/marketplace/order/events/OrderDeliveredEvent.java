package edu.fi.muni.cz.marketplace.order.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.Value;

@Value
public class OrderDeliveredEvent {

  UUID orderId;
  String sellerStripeAccountId;
  BigDecimal payoutAmount;
  BigDecimal commission;
  Instant deliveredAt;
}
