package edu.fi.muni.cz.marketplace.order.events;

import java.time.Instant;

import lombok.Value;

@Value
public class OrderCompletedEvent {

  private String payoutTransferId;
  private String commissionTransferId;
  private Instant completedAt;

}
