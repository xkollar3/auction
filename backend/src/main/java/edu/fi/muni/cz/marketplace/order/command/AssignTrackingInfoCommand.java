package edu.fi.muni.cz.marketplace.order.command;

import java.time.Instant;
import java.util.UUID;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class AssignTrackingInfoCommand {

  @TargetAggregateIdentifier
  UUID orderId;
  String trackingNumber;
  String ship24TrackerId;
  Instant enteredAt;
}
