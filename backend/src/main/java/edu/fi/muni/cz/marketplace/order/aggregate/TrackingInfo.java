package edu.fi.muni.cz.marketplace.order.aggregate;

import java.time.Instant;

import javax.annotation.Nullable;

import lombok.Value;

@Value
public class TrackingInfo {

  String trackingNumber;
  String ship24TrackerId;
  Instant createdAt;

  @Nullable
  TrackingStatusMilestone statusMilestone;

  @Nullable
  String lastEventStatus;

  @Nullable
  Instant lastEventOccurredAt;
}
