package edu.fi.muni.cz.marketplace.user.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import edu.fi.muni.cz.marketplace.user.command.CompleteStripeCustomerCreationCommand;
import edu.fi.muni.cz.marketplace.user.command.CreateStripeCustomerCommand;
import edu.fi.muni.cz.marketplace.user.command.FailStripeCustomerCreationCommand;
import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.event.StripeCustomerCreationFailedEvent;
import edu.fi.muni.cz.marketplace.user.event.StripeCustomerCreationInitiatedEvent;
import edu.fi.muni.cz.marketplace.user.event.StripeCustomerCreationSuccessEvent;
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

  private StripeCustomerStatus stripeCustomerStatus;
  private String stripeCustomerId;
  private String stripeCustomerCreationError;

  @CommandHandler
  public User(RegisterUserCommand command) {
    apply(new UserRegisteredEvent(
        command.getId(), command.getKeycloakUserId()));
  }

  @EventSourcingHandler
  public void on(UserRegisteredEvent event) {
    this.id = event.getId();
    this.keycloakUserId = event.getKeycloakUserId();
    this.stripeCustomerStatus = StripeCustomerStatus.NOT_CREATED;
  }

  @CommandHandler
  public void handle(CreateStripeCustomerCommand command) {
    apply(new StripeCustomerCreationInitiatedEvent(
        command.getId(),
        command.getEmail(),
        command.getName(),
        command.getPhone(),
        command.getShippingAddress()));
  }

  @EventSourcingHandler
  public void on(StripeCustomerCreationInitiatedEvent event) {
    this.stripeCustomerStatus = StripeCustomerStatus.PENDING;
    this.stripeCustomerCreationError = null;
  }

  @CommandHandler
  public void handle(CompleteStripeCustomerCreationCommand command) {
    apply(new StripeCustomerCreationSuccessEvent(
        command.getId(),
        command.getStripeCustomerId()));
  }

  @EventSourcingHandler
  public void on(StripeCustomerCreationSuccessEvent event) {
    this.stripeCustomerStatus = StripeCustomerStatus.SUCCESS;
    this.stripeCustomerId = event.getStripeCustomerId();
    this.stripeCustomerCreationError = null;
  }

  @CommandHandler
  public void handle(FailStripeCustomerCreationCommand command) {
    apply(new StripeCustomerCreationFailedEvent(
        command.getId(),
        command.getErrorMessage()));
  }

  @EventSourcingHandler
  public void on(StripeCustomerCreationFailedEvent event) {
    this.stripeCustomerStatus = StripeCustomerStatus.FAILED;
    this.stripeCustomerCreationError = event.getErrorMessage();
  }

}
