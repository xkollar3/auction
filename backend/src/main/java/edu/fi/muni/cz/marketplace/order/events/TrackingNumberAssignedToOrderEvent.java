package edu.fi.muni.cz.marketplace.order.events;

import java.time.Instant;
import java.util.UUID;
import lombok.Value;

@Value
public class TrackingNumberAssignedToOrderEvent {

  UUID orderId;
  String trackingNumber;
  String ship24TrackerId;
  Instant enteredAt;
}
