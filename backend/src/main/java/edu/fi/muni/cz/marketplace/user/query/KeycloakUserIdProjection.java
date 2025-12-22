package edu.fi.muni.cz.marketplace.user.query;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.user.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ProcessingGroup("keycloak_user_ids")
@RequiredArgsConstructor
public class KeycloakUserIdProjection {

  private final KeycloakUserIdRepository repository;

  @EventHandler
  public void on(UserRegisteredEvent event) {
    log.info("Processing UserRegisteredEvent for Keycloak user ID: {}", event.getKeycloakUserId());

    KeycloakUserIdReadModel readModel = new KeycloakUserIdReadModel(
        event.getId(),
        event.getKeycloakUserId()
    );

    repository.save(readModel);
    log.info("Saved Keycloak user ID lookup for aggregate ID: {}", event.getId());
  }
}
