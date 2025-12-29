package edu.fi.muni.cz.marketplace.order.command;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class ReserveFundsCommand {

  @TargetAggregateIdentifier
  UUID id;
  String customerId;
  String paymentMethodId;
  BigDecimal amount;

}
