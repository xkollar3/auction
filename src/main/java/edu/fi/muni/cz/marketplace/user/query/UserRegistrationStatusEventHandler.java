package edu.fi.muni.cz.marketplace.user.query;

import edu.fi.muni.cz.marketplace.user.event.UserKeycloakIdAssignedEvent;
import edu.fi.muni.cz.marketplace.user.event.UserRegisteredEvent;
import edu.fi.muni.cz.marketplace.user.event.UserRegistrationFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ProcessingGroup("user_registration_status")
public class UserRegistrationStatusEventHandler {

  private final UserRegistrationStatusRepository repository;
  private final QueryUpdateEmitter queryUpdateEmitter;

  @EventHandler
  public void on(UserRegisteredEvent event) {
    log.info("Creating registration status entry for user {}", event.getId());

    UserRegistrationStatusReadModel readModel = UserRegistrationStatusReadModel.builder()
        .id(event.getId())
        .nickname(event.getNickname().toFullString())
        .firstName(event.getFirstName())
        .lastName(event.getLastName())
        .email(event.getEmail())
        .phoneNumber(event.getPhoneNumber())
        .status(RegistrationStatus.PENDING)
        .build();

    repository.save(readModel);
  }

  @EventHandler
  public void on(UserKeycloakIdAssignedEvent event) {
    log.info("Updating registration status with Keycloak ID for user {}", event.getId());

    repository.findById(event.getId()).ifPresent(readModel -> {
      readModel.setKeycloakUserId(event.getKeycloakUserId());
      readModel.setStatus(RegistrationStatus.COMPLETED);
      repository.save(readModel);

      // Emit update to subscription queries
      queryUpdateEmitter.emit(
          FindUserRegistrationStatusQuery.class,
          query -> query.getId().equals(event.getId()),
          readModel);
    });
  }

  @EventHandler
  public void on(UserRegistrationFailedEvent event) {
    log.info("Marking registration as failed for user {}", event.getId());

    repository.findById(event.getId()).ifPresent(readModel -> {
      readModel.setStatus(RegistrationStatus.FAILED);
      readModel.setErrorMessage(event.getErrorMessage());
      repository.save(readModel);

      // Emit update to subscription queries
      queryUpdateEmitter.emit(
          FindUserRegistrationStatusQuery.class,
          query -> query.getId().equals(event.getId()),
          readModel);
    });
  }

  @QueryHandler
  public UserRegistrationStatusReadModel handle(FindUserRegistrationStatusQuery query) {
    return repository.findById(query.getId()).orElse(null);
  }
}
