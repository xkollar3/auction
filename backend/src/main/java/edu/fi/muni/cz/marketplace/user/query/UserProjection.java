package edu.fi.muni.cz.marketplace.user.query;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.user.event.StripeCustomerCreatedEvent;
import edu.fi.muni.cz.marketplace.user.event.StripeSellerAccountCreatedEvent;
import edu.fi.muni.cz.marketplace.user.event.StripeSellerStatusUpdatedEvent;
import edu.fi.muni.cz.marketplace.user.event.UserRegisteredEvent;
import edu.fi.muni.cz.marketplace.user.event.UserRemovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ProcessingGroup("user_read_model")
@RequiredArgsConstructor
public class UserProjection {

  private final UserReadModelRepository repository;

  @EventHandler
  public void on(UserRegisteredEvent event) {
    log.info("Processing UserRegisteredEvent for Keycloak user ID: {}", event.getKeycloakUserId());

    UserReadModel readModel = new UserReadModel(
        event.getId(),
        event.getKeycloakUserId(),
        null,
        null,
        false);

    repository.save(readModel);
    log.info("Saved Keycloak user ID lookup for aggregate ID: {}", event.getId());
  }

  @EventHandler
  public void on(StripeCustomerCreatedEvent event) {
    log.info("Processing StripeCustomerCreatedEvent for aggregate ID: {}", event.getId());

    repository.findById(event.getId()).ifPresent(user -> {
      user.setStripeCustomerId(event.getStripeCustomerId());
      repository.save(user);
      log.info("Updated Stripe Customer ID for aggregate ID: {}", event.getId());
    });
  }

  @EventHandler
  public void on(StripeSellerAccountCreatedEvent event) {
    log.info("Processing StripeSellerAccountCreatedEvent for aggregate ID: {}", event.getId());

    repository.findById(event.getId()).ifPresent(user -> {
      user.setStripeSellerAccountId(event.getStripeSellerAccountId());
      repository.save(user);
      log.info("Updated Stripe Seller Account ID for aggregate ID: {}", event.getId());
    });
  }

  @EventHandler
  public void on(StripeSellerStatusUpdatedEvent event) {
    log.info("Processing StripeSellerStatusUpdatedEvent for aggregate ID: {}", event.getId());

    repository.findById(event.getId()).ifPresent(user -> {
      user.setSellerAccountEnabled(event.isEnabled());
      repository.save(user);
      log.info("Updated seller account status to {} for aggregate ID: {}", event.isEnabled(), event.getId());
    });
  }

  @EventHandler
  public void on(UserRemovedEvent event) {
    log.info("Processing UserRemovedEvent for aggregate ID: {}", event.getId());
    repository.deleteById(event.getId());
    log.info("Deleted user from read model for aggregate ID: {}", event.getId());
  }

  @QueryHandler
  public UserReadModel handle(FindUserByKeycloakIdQuery query) {
    return repository.findByKeycloakUserId(query.getKeycloakUserId())
        .orElse(null);
  }
}
