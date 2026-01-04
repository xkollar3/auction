package edu.fi.muni.cz.marketplace.order.client.dto;

import java.time.Instant;

public record CreateTrackerResponse(
    Data data
) {

  public record Data(
      Tracker tracker
  ) {
  }

  public record Tracker(
      String trackerId,
      String trackingNumber,
      String shipmentReference,
      Instant createdAt
  ) {
  }
}
