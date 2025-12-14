package edu.fi.muni.cz.marketplace.user.event;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserKeycloakIdAssignedEvent {

  UUID id;
  String keycloakUserId;
}
