package edu.fi.muni.cz.marketplace.user.query;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeycloakUserIdRepository extends JpaRepository<KeycloakUserIdReadModel, UUID> {

  boolean existsByKeycloakUserId(String keycloakUserId);
}
