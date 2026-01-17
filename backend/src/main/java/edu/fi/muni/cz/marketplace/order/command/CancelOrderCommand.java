package edu.fi.muni.cz.marketplace.order.command;

import java.util.UUID;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class CancelOrderCommand {

  @TargetAggregateIdentifier
  UUID orderId;
  String refundId;
}
