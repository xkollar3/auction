package edu.fi.muni.cz.marketplace.user.event;

import java.util.UUID;

import edu.fi.muni.cz.marketplace.user.aggregate.UserNickname;
import lombok.Value;

@Value
public class UserRegisteredEvent {

  UUID id;
  UserNickname nickname;
  String firstName;
  String lastName;
  String email;
  String phoneNumber;
  String password;
}
