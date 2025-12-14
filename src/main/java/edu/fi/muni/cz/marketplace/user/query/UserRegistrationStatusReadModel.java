package edu.fi.muni.cz.marketplace.user.query;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "user_registration_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationStatusReadModel {

  @Id
  private UUID id;

  private String nickname;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private String keycloakUserId;

  @Enumerated(EnumType.STRING)
  private RegistrationStatus status;

  private String errorMessage;
}
