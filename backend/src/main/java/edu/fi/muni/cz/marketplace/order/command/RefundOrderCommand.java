package edu.fi.muni.cz.marketplace.order.command;

import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class RefundOrderCommand {

  @TargetAggregateIdentifier
  UUID orderId;
  String paymentIntentId;
}
