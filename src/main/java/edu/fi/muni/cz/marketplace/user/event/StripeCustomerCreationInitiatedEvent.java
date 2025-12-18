package edu.fi.muni.cz.marketplace.user.event;

import java.util.UUID;

import edu.fi.muni.cz.marketplace.user.aggregate.Address;
import lombok.Value;

@Value
public class StripeCustomerCreationInitiatedEvent {

  UUID id;
  String email;
  String name;
  String phone;
  Address shippingAddress;
}
