package edu.fi.muni.cz.marketplace.user.command;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
@Builder
public class FailUserRegistrationCommand {

  @TargetAggregateIdentifier
  UUID id;
  String errorMessage;
}
