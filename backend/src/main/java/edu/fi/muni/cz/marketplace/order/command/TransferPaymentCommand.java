package edu.fi.muni.cz.marketplace.order.command;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class TransferPaymentCommand {

  @TargetAggregateIdentifier
  private UUID orderId;
  private String stripeAccountId;
  private BigDecimal amount;
}
