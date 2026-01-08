package edu.fi.muni.cz.marketplace.user.query;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReadModelRepository extends JpaRepository<UserReadModel, UUID> {

  boolean existsByKeycloakUserId(String keycloakUserId);

  Optional<UserReadModel> findByKeycloakUserId(String keycloakUserId);
}
