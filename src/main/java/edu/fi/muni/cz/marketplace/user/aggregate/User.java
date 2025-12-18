package edu.fi.muni.cz.marketplace.user.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.event.UserRegisteredEvent;
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
    apply(new UserRegisteredEvent(
        command.getId(), command.getKeycloakUserId()));
  }

  @EventSourcingHandler
  public void on(UserRegisteredEvent event) {
    this.id = event.getId();
    this.keycloakUserId = event.getKeycloakUserId();
  }

}
