package edu.fi.muni.cz.marketplace.user.handler;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.user.event.UserRegisteredEvent;
import edu.fi.muni.cz.marketplace.user.event.UserRemovedEvent;
import edu.fi.muni.cz.marketplace.user.persistence.KeycloakUserLookup;
import edu.fi.muni.cz.marketplace.user.persistence.KeycloakUserLookupRepository;

/**
 * Subscribing processor that updates lookup table with Keycloak user IDs.
 * This lookup table is owned by the command side for set-based validation.
 */
@Component
@ProcessingGroup("keycloakUserLookup")
public class UserLookupEventHandler {

  @EventHandler
  public void on(UserRegisteredEvent event, KeycloakUserLookupRepository repository) {
    repository.save(new KeycloakUserLookup(event.getKeycloakUserId(), event.getId()));
  }

  @EventHandler
  public void on(UserRemovedEvent event, KeycloakUserLookupRepository repository) {
    repository.deleteById(event.getKeycloakUserId());
  }
}
