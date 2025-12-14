package edu.fi.muni.cz.marketplace.user.dto;

import edu.fi.muni.cz.marketplace.user.query.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationResponse {

  private UUID id;
  private String nickname;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private String keycloakUserId;
  private RegistrationStatus status;
  private String errorMessage;
}
