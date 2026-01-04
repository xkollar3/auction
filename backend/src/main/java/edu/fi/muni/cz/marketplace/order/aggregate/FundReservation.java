package edu.fi.muni.cz.marketplace.order.aggregate;

import java.math.BigDecimal;
import java.time.Instant;
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

  // net amount, because of deductions from stripe
  @Nonnull
  BigDecimal netAmount;

  @Nonnull
  Instant reservedAt;

  @Nonnull
  String sellerId;

  @Nonnull
  String sellerStripeAccountId;
}
