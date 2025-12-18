package edu.fi.muni.cz.marketplace.user.command;

import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import edu.fi.muni.cz.marketplace.user.aggregate.Address;
import lombok.Value;

@Value
public class CreateStripeCustomerCommand {

  @TargetAggregateIdentifier
  UUID id;
  String email;
  String name;
  String phone;
  Address shippingAddress;
}
