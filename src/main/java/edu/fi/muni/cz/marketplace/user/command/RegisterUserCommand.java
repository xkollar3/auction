package edu.fi.muni.cz.marketplace.user.command;

import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
@Builder
public class RegisterUserCommand {

  @TargetAggregateIdentifier
  String userId;
  String firstName;
  String lastName;
  String email;
  String phoneNumber;
}
