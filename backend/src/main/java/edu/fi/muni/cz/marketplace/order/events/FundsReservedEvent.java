package edu.fi.muni.cz.marketplace.order.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.Value;

@Value
public class FundsReservedEvent {

  UUID orderId;
  String paymentIntentId;
  String paymentMethodId;
  String deadlineId;
  BigDecimal amount;
  Instant reservedAt;
}
