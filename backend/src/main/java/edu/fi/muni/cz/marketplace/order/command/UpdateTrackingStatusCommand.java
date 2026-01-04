package edu.fi.muni.cz.marketplace.order.command;

import edu.fi.muni.cz.marketplace.order.aggregate.TrackingStatusMilestone;
import java.time.Instant;
import java.util.UUID;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class UpdateTrackingStatusCommand {

  @TargetAggregateIdentifier
  UUID orderId;
  String eventId;
  TrackingStatusMilestone statusMilestone;
  String eventStatus;
  Instant eventOccurredAt;
}
