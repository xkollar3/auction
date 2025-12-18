package edu.fi.muni.cz.marketplace.user.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.event.UserRegistrationInitiatedEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Aggregate
@NoArgsConstructor
public class User {

  @AggregateIdentifier
  private UUID id;

  private String keycloakUserId;

  @CommandHandler
  public User(RegisterUserCommand command) {
    apply(new UserRegistrationInitiatedEvent(
        command.getId(), command.getKeycloakUserId(), command.getPhoneNumber()));
  }

  @EventSourcingHandler
  public void on(UserRegistrationInitiatedEvent event) {
    this.id = event.getId();
  }

}
