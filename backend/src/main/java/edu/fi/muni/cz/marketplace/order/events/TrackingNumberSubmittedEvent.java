package edu.fi.muni.cz.marketplace.order.events;

import java.util.UUID;
import lombok.Value;

@Value
public class TrackingNumberSubmittedEvent {

  UUID orderId;
  UUID enteredByUserId;
  String trackingNumber;
}
