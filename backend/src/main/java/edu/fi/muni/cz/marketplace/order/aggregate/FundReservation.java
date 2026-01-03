package edu.fi.muni.cz.marketplace.order.aggregate;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Value;

@Value
public class FundReservation {

  String paymentIntentId;
  String paymentMethodId;
  String deadlineId;
  BigDecimal amount;
  Instant reservedAt;

  String sellerId;
  String sellerStripeAccountId;
}
