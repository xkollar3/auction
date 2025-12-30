package edu.fi.muni.cz.marketplace.order.deadline;

import java.util.UUID;

import lombok.Value;

@Value
public class ShippingDeadlineNotMetPayload {

  UUID orderId;
  String paymentIntentId;
}
