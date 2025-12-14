package edu.fi.muni.cz.marketplace.user.aggregate;

import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.event.UserRegisteredEvent;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
@NoArgsConstructor
public class User {

  @AggregateIdentifier
  private String userId;

  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;

  @CommandHandler
  public User(RegisterUserCommand command) {
    apply(UserRegisteredEvent.builder()
        .userId(command.getUserId())
        .firstName(command.getFirstName())
        .lastName(command.getLastName())
        .email(command.getEmail())
        .phoneNumber(command.getPhoneNumber())
        .build());
  }

  @EventSourcingHandler
  public void on(UserRegisteredEvent event) {
    this.userId = event.getUserId();
    this.firstName = event.getFirstName();
    this.lastName = event.getLastName();
    this.email = event.getEmail();
    this.phoneNumber = event.getPhoneNumber();
  }
}
