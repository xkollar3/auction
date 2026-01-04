package edu.fi.muni.cz.marketplace.order.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class AssignFundReservationCommand {

  @TargetAggregateIdentifier
  UUID orderId;
  String paymentIntentId;
  String paymentMethodId;
  BigDecimal grossAmount;
  Instant reservedAt;
  UUID sellerId;
  String sellerStripeAccountId;
}
