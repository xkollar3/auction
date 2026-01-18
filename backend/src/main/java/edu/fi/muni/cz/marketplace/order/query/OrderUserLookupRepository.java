package edu.fi.muni.cz.marketplace.order.query;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderUserLookupRepository extends JpaRepository<OrderUserLookup, UUID> {

  Optional<OrderUserLookup> findByKeycloakUserId(String keycloakUserId);
}
