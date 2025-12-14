package edu.fi.muni.cz.marketplace.user.event;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserRegisteredEvent {

  String userId;
  String firstName;
  String lastName;
  String email;
  String phoneNumber;
}
