package edu.fi.muni.cz.marketplace.user.command;

import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class UpdateStripeSellerStatusCommand {
  @TargetAggregateIdentifier
  UUID id;
  boolean enabled;
}
