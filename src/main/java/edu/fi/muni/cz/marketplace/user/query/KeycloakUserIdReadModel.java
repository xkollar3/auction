package edu.fi.muni.cz.marketplace.user.query;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "keycloak_user_id_lookup")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserIdReadModel {

  @Id
  private UUID id;

  @Column(unique = true, nullable = false)
  private String keycloakUserId;
}
