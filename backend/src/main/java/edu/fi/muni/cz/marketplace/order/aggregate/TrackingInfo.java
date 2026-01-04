package edu.fi.muni.cz.marketplace.order.aggregate;

import java.time.Instant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Value;

@Value
public class TrackingInfo {

  @Nonnull
  String trackingNumber;

  @Nonnull
  String ship24TrackerId;

  @Nonnull
  Instant createdAt;

  @Nullable
  TrackingStatusMilestone statusMilestone;

  @Nullable
  String lastEventStatus;

  @Nullable
  Instant lastEventOccurredAt;
}
