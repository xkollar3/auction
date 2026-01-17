package edu.fi.muni.cz.marketplace.order.events;

import java.util.UUID;
import lombok.Value;

@Value
public class TrackingNumberEnteredEvent {

  UUID orderId;
  String trackingNumber;
  String trackerId;
}
