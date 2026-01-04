package edu.fi.muni.cz.marketplace.order.aggregate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import javax.annotation.Nonnull;
import lombok.Value;

@Value
public class FundReservation {

  @Nonnull
  String paymentIntentId;

  @Nonnull
  String paymentMethodId;

  @Nonnull
  String deadlineId;

  @Nonnull
  BigDecimal grossAmount;

  @Nonnull
  Instant reservedAt;

  @Nonnull
  UUID sellerId;

  @Nonnull
  String sellerStripeAccountId;
}
