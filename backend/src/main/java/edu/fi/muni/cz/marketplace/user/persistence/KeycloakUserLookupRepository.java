package edu.fi.muni.cz.marketplace.user.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeycloakUserLookupRepository extends JpaRepository<KeycloakUserLookup, String> {

  Optional<KeycloakUserLookup> findByUserId(UUID userId);
}
