package edu.fi.muni.cz.marketplace.order.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class AssignFundReservationCommand {

  @TargetAggregateIdentifier
  UUID orderId;
  String paymentIntentId;
  String paymentMethodId;
  BigDecimal netAmount;
  Instant reservedAt;
  String sellerId;
  String sellerStripeAccountId;
}
