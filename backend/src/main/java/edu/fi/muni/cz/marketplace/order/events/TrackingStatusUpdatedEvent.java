package edu.fi.muni.cz.marketplace.order.events;

import java.time.Instant;
import java.util.UUID;

import edu.fi.muni.cz.marketplace.order.aggregate.TrackingStatusMilestone;
import lombok.Value;

@Value
public class TrackingStatusUpdatedEvent {

  UUID orderId;
  String eventId;
  TrackingStatusMilestone statusMilestone;
  String eventStatus;
  Instant eventOccurredAt;
}
