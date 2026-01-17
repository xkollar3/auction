package edu.fi.muni.cz.marketplace.order.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Value;

@Value
public class FundReservationInformationAssignedEvent {

  UUID orderId;
  UUID buyerId;
  String paymentIntentId;
  String paymentMethodId;
  String deadlineId;
  BigDecimal amount;
  Instant reservedAt;
  UUID sellerId;
  String sellerAccountId;
}
