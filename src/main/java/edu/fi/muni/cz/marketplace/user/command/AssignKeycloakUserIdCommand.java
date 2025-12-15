package edu.fi.muni.cz.marketplace.user.command;

import lombok.Value;

import java.util.UUID;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import edu.fi.muni.cz.marketplace.user.aggregate.UserNickname;

@Value
public class AssignKeycloakUserIdCommand {

  @TargetAggregateIdentifier
  UUID id;
  String keycloakUserId;
  UserNickname nickname;

}
