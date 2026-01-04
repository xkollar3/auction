package edu.fi.muni.cz.marketplace.order.command;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class ReserveFundsCommand {

  @TargetAggregateIdentifier
  UUID id;
  String customerId;
  String paymentMethodId;
  BigDecimal amount;
  UUID sellerId;
  String sellerStripeAccountId;

}
