package edu.fi.muni.cz.marketplace.order.events;

import lombok.Value;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
public class FundsReservedEvent {

  UUID orderId;
  UUID buyerId;
  String paymentIntentId;
  String paymentMethodId;
  BigDecimal grossAmount;
  Instant reservedAt;
  UUID sellerId;
  String sellerStripeAccountId;
}
