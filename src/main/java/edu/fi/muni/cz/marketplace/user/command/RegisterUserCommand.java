package edu.fi.muni.cz.marketplace.user.command;

import lombok.Value;

import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class RegisterUserCommand {

  @TargetAggregateIdentifier
  UUID id;
  String keycloakUserId;
}
