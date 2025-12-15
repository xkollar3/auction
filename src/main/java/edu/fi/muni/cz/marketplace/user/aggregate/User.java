package edu.fi.muni.cz.marketplace.user.aggregate;

import edu.fi.muni.cz.marketplace.user.command.AssignKeycloakUserIdCommand;
import edu.fi.muni.cz.marketplace.user.command.FailUserRegistrationCommand;
import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.event.UserKeycloakIdAssignedEvent;
import edu.fi.muni.cz.marketplace.user.event.UserRegistrationInitiatedEvent;
import edu.fi.muni.cz.marketplace.user.event.UserRegistrationFailedEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.util.UUID;

@Getter
@Setter
@Aggregate
@NoArgsConstructor
public class User {

  @AggregateIdentifier
  private UUID id;

  private UserNickname nickname;

  private String keycloakUserId;
  private RegistrationStatus registrationStatus;

  @CommandHandler
  public User(RegisterUserCommand command) {
    apply(new UserRegistrationInitiatedEvent(
        command.getId(),
        new UserNickname(command.getNickname()),
        command.getFirstName(),
        command.getLastName(),
        command.getEmail(),
        command.getPhoneNumber(),
        command.getPassword()));
  }

  @EventSourcingHandler
  public void on(UserRegistrationInitiatedEvent event) {
    this.id = event.getId();
    this.nickname = event.getNickname();
    this.registrationStatus = RegistrationStatus.PENDING;
  }

  @CommandHandler
  public void handle(AssignKeycloakUserIdCommand command) {
    apply(new UserKeycloakIdAssignedEvent(command.getId(), command.getKeycloakUserId(), command.getNickname()));
  }

  @EventSourcingHandler
  public void on(UserKeycloakIdAssignedEvent event) {
    this.keycloakUserId = event.getKeycloakUserId();
    this.registrationStatus = RegistrationStatus.COMPLETED;
  }

  @CommandHandler
  public void handle(FailUserRegistrationCommand command) {
    apply(new UserRegistrationFailedEvent(command.getId(), command.getErrorMessage(), command.getHttpStatus()));
  }

  @EventSourcingHandler
  public void on(UserRegistrationFailedEvent event) {
    this.registrationStatus = RegistrationStatus.FAILED;
  }
}
