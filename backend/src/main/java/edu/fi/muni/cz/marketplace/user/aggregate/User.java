package edu.fi.muni.cz.marketplace.user.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import edu.fi.muni.cz.marketplace.user.command.AssignStripeCustomerIdCommand;
import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.event.StripeCustomerCreatedEvent;
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

  /**
   * String identifier, format cus_xxxx
   *
   * Used to add payment methods that can be charged
   *
   * Has to be defined if user wants to add a payment method and bid
   *
   * Added by flow from CreateStripeCustomerCommnad
   **/
  private String stripeCustomerId;

  /**
   * String identifier, format pi_xxxx
   *
   * Refers to a payment method, in this case always expect it to be active and
   * usable
   *
   * Has to be defined after customerId, user who has both can bid
   *
   * Added directly by AddPaymentInformationCommand
   **/
  private String stripePaymentMethodId;

  /**
   * String identifier, format acc_xxxx
   *
   * Used as destination to which funds can be transferred
   *
   * Has to be defined if user wants to sell on the platform
   *
   * Added by flow from CreateStripeConnectedAccountCommand
   **/
  private String stripeSellerAccountId;

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

  @CommandHandler
  public void on(AssignStripeCustomerIdCommand command) {
    if (stripeCustomerId != null) {
      throw new IllegalStateException(
          String.format("User with id: %s, already has a customer account", command.getId()));
    }
    apply(new StripeCustomerCreatedEvent(command.getId(), command.getStripeCustomerId()));
  }

  @EventSourcingHandler
  public void on(StripeCustomerCreatedEvent event) {
    this.stripeCustomerId = event.getStripeCustomerId();
  }
}
