package edu.fi.muni.cz.marketplace.user.event;

import java.util.UUID;

import lombok.Value;

@Value
public class UserRegistrationFailedEvent {

  UUID id;
  String errorMessage;
  Integer httpStatus;
}
