package edu.fi.muni.cz.marketplace.order.events;

import java.time.Instant;
import java.util.UUID;
import lombok.Value;

@Value
public class OrderCancelledEvent {

  UUID orderId;
  String refundId;
  Instant cancelledAt;
}
