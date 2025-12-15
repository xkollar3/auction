package edu.fi.muni.cz.marketplace.user.saga;

import edu.fi.muni.cz.marketplace.user.command.AssignKeycloakUserIdCommand;
import edu.fi.muni.cz.marketplace.user.command.FailUserRegistrationCommand;
import edu.fi.muni.cz.marketplace.user.event.UserKeycloakIdAssignedEvent;
import edu.fi.muni.cz.marketplace.user.event.UserRegistrationFailedEvent;
import edu.fi.muni.cz.marketplace.user.event.UserRegistrationInitiatedEvent;
import edu.fi.muni.cz.marketplace.user.exception.KeycloakRegistrationFailedException;
import edu.fi.muni.cz.marketplace.user.service.KeycloakUserService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Slf4j
@Saga
@NoArgsConstructor
public class UserRegistrationSaga {

  @Autowired
  private transient KeycloakUserService keycloakUserService;

  @Autowired
  private transient CommandGateway commandGateway;

  @StartSaga
  @SagaEventHandler(associationProperty = "id")
  public void on(UserRegistrationInitiatedEvent event) {
    UUID userId = event.getId();
    log.info("Starting user registration saga for user ID: {}", userId);

    try {
      String keycloakUserId = keycloakUserService.createUser(
          event.getEmail(),
          event.getFirstName(),
          event.getLastName(),
          event.getNickname().toFullString(),
          event.getPassword());

      log.info("Keycloak user created successfully with ID: {} for user: {}", keycloakUserId, userId);
      commandGateway.send(new AssignKeycloakUserIdCommand(userId, keycloakUserId, event.getNickname()));
    } catch (KeycloakRegistrationFailedException e) {
      commandGateway.send(new FailUserRegistrationCommand(userId, e.getMessage(), e.getStatus().value()));
    } catch (Exception e) {
      commandGateway
          .send(new FailUserRegistrationCommand(userId, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  @EndSaga
  @SagaEventHandler(associationProperty = "id")
  public void on(UserKeycloakIdAssignedEvent event) {
    log.info("User registration completed successfully for user ID: {}", event.getId());
  }

  @EndSaga
  @SagaEventHandler(associationProperty = "id")
  public void on(UserRegistrationFailedEvent event) {
    log.info("User registration failed for user ID: {} with error: {}", event.getId(), event.getErrorMessage());
  }
}
