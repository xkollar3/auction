package edu.fi.muni.cz.marketplace.order.command;

import java.time.Instant;
import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import edu.fi.muni.cz.marketplace.order.aggregate.TrackingStatusMilestone;
import lombok.Value;

@Value
public class UpdateTrackingStatusCommand {

  @TargetAggregateIdentifier
  UUID orderId;
  String eventId;
  TrackingStatusMilestone statusMilestone;
  String eventStatus;
  Instant eventOccurredAt;
}
