package edu.fi.muni.cz.marketplace.order.events;

import java.time.Instant;
import java.util.UUID;

import lombok.Value;

@Value
public class OrderCompletedEvent {

  private UUID orderId;
  private String payoutTransferId;
  private String commissionTransferId;
  private Instant completedAt;

}
