package edu.fi.muni.cz.marketplace.order.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Value;

@Value
public class AssignFundReservationInformationCommand {

  UUID orderId;
  UUID buyerId;
  String paymentIntentId;
  String paymentMethodId;
  BigDecimal grossAmount;
  Instant reservedAt;
  UUID sellerId;
  String sellerStripeAccountId;
}
