package edu.fi.muni.cz.marketplace.order.dto;

import edu.fi.muni.cz.marketplace.order.aggregate.TrackingStatusMilestone;
import java.time.Instant;
import java.util.List;

public record Ship24WebhookPayload(
    List<TrackingUpdate> trackings
) {

  public record TrackingUpdate(
      Tracker tracker,
      List<TrackingEvent> events
  ) {
  }

  public record Tracker(
      String trackerId,
      String trackingNumber,
      String shipmentReference
  ) {
  }

  public record TrackingEvent(
      String eventId,
      String status,
      Instant occurrenceDatetime,
      String statusCode,
      String statusCategory,
      StatusMilestone statusMilestone
  ) {
  }

  public enum StatusMilestone {
    info_received,
    in_transit,
    out_for_delivery,
    failed_attempt,
    available_for_pickup,
    delivered,
    exception,
    pending;

    public TrackingStatusMilestone toTrackingStatusMilestone() {
      return switch (this) {
        case info_received -> TrackingStatusMilestone.INFO_RECEIVED;
        case in_transit -> TrackingStatusMilestone.IN_TRANSIT;
        case out_for_delivery -> TrackingStatusMilestone.OUT_FOR_DELIVERY;
        case failed_attempt -> TrackingStatusMilestone.FAILED_ATTEMPT;
        case available_for_pickup -> TrackingStatusMilestone.AVAILABLE_FOR_PICKUP;
        case delivered -> TrackingStatusMilestone.DELIVERED;
        case exception -> TrackingStatusMilestone.EXCEPTION;
        case pending -> TrackingStatusMilestone.PENDING;
      };
    }
  }
}

