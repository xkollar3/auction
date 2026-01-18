package edu.fi.muni.cz.marketplace.order.query;

import edu.fi.muni.cz.marketplace.user.event.UserRegisteredEvent;
import edu.fi.muni.cz.marketplace.user.event.UserRemovedEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ProcessingGroup("order_user_lookup")
@RequiredArgsConstructor
public class OrderUserLookupProjection {

  private final OrderUserLookupRepository repository;

  @EventHandler
  public void on(UserRegisteredEvent event) {
    log.info("Processing UserRegisteredEvent for user ID: {}, Keycloak ID: {}",
        event.getId(), event.getKeycloakUserId());

    OrderUserLookup lookup = new OrderUserLookup(event.getId(), event.getKeycloakUserId());
    repository.save(lookup);

    log.info("Created user lookup entry for user ID: {}", event.getId());
  }

  @EventHandler
  public void on(UserRemovedEvent event) {
    log.info("Processing UserRemovedEvent for user ID: {}", event.getId());

    repository.deleteById(event.getId());

    log.info("Deleted user lookup entry for user ID: {}", event.getId());
  }

  @QueryHandler
  public UUID handle(FindUserIdByKeycloakIdQuery query) {
    log.debug("Looking up user ID for Keycloak ID: {}", query.getKeycloakUserId());
    return repository.findByKeycloakUserId(query.getKeycloakUserId())
        .map(OrderUserLookup::getUserId)
        .orElse(null);
  }
}
