package edu.fi.muni.cz.marketplace.user.persistence;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "keycloak_user_lookup")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserLookup {

  @Id
  private String keycloakUserId;

  private UUID userId;
}
