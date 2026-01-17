package edu.fi.muni.cz.marketplace.order.command;

import java.util.UUID;
import lombok.Value;

@Value
public class RegisterTrackingNumberCommand {

  UUID orderId;
  UUID enteredByUserId;
  String trackingNumber;
}
