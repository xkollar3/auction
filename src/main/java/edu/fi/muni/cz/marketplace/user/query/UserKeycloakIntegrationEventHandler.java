package edu.fi.muni.cz.marketplace.user.query;

import edu.fi.muni.cz.marketplace.user.command.AssignKeycloakUserIdCommand;
import edu.fi.muni.cz.marketplace.user.command.FailUserRegistrationCommand;
import edu.fi.muni.cz.marketplace.user.event.UserRegisteredEvent;
import edu.fi.muni.cz.marketplace.user.service.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ProcessingGroup("keycloak_integration")
public class UserKeycloakIntegrationEventHandler {

  private final KeycloakUserService keycloakUserService;
  private final CommandGateway commandGateway;

  @EventHandler
  public void on(UserRegisteredEvent event) {
    UUID eventId = event.getId();
    log.info("Creating Keycloak user for aggregate ID: {}", eventId);

    if (eventId == null) {
      log.error("CRITICAL: UserRegisteredEvent has NULL id! Event details: nickname={}, email={}",
          event.getNickname(), event.getEmail());
      return;
    }

    try {
      String keycloakUserId = keycloakUserService.createUser(
          event.getEmail(),
          event.getFirstName(),
          event.getLastName(),
          event.getNickname().toFullString(),
          event.getPassword());

      log.info("Assigning Keycloak user ID {} to aggregate {}", keycloakUserId, eventId);
      commandGateway.send(AssignKeycloakUserIdCommand.builder()
          .id(eventId)
          .keycloakUserId(keycloakUserId)
          .build());
    } catch (Exception e) {
      log.error("Failed to create Keycloak user for aggregate ID: {}", eventId, e);
      String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
      log.info("Dispatching FailUserRegistrationCommand with id={}, error={}", eventId, errorMsg);
      commandGateway.send(FailUserRegistrationCommand.builder()
          .id(eventId)
          .errorMessage(errorMsg)
          .build());
    }
  }
}
