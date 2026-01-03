package edu.fi.muni.cz.marketplace.order.command;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class DeductCommissionCommand {

  @TargetAggregateIdentifier
  private UUID orderId;
  private BigDecimal commision;
}
