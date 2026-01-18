package edu.fi.muni.cz.marketplace.user.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import edu.fi.muni.cz.marketplace.user.command.AddPaymentInformationCommand;
import edu.fi.muni.cz.marketplace.user.command.AssignStripeSellerAccountIdCommand;
import edu.fi.muni.cz.marketplace.user.command.AssignStripeCustomerIdCommand;
import edu.fi.muni.cz.marketplace.user.command.RegisterUserCommand;
import edu.fi.muni.cz.marketplace.user.command.RemoveUserCommand;
import edu.fi.muni.cz.marketplace.user.command.UpdateStripeSellerStatusCommand;
import edu.fi.muni.cz.marketplace.user.event.PaymentInformationAddedEvent;
import edu.fi.muni.cz.marketplace.user.event.StripeCustomerCreatedEvent;
import edu.fi.muni.cz.marketplace.user.event.StripeSellerAccountCreatedEvent;
import edu.fi.muni.cz.marketplace.user.event.StripeSellerStatusUpdatedEvent;
import edu.fi.muni.cz.marketplace.user.event.UserRegisteredEvent;
import edu.fi.muni.cz.marketplace.user.event.UserRemovedEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Aggregate for handling user information
 *
 * @author drozdma6
 **/
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

  /**
   * True if seller account is enabled
   *
   * Added by flow from UpdateStripeSellerStatusCommand
   */
  private boolean sellerAccountEnabled;

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

  @CommandHandler
  public void on(AddPaymentInformationCommand command) {
    if (stripeCustomerId == null) {
      throw new IllegalStateException(
          String.format("User with id: %s, does not have a customer account", command.getId()));
    }
    apply(new PaymentInformationAddedEvent(command.getId(), command.getPaymentMethodId()));
  }

  @EventSourcingHandler
  public void on(PaymentInformationAddedEvent event) {
    this.stripePaymentMethodId = event.getPaymentMethodId();
  }

  @CommandHandler
  public void on(AssignStripeSellerAccountIdCommand command) {
    if (stripeSellerAccountId != null) {
      throw new IllegalStateException(
          String.format("User with id: %s, already has a seller account", command.getId()));
    }
    apply(new StripeSellerAccountCreatedEvent(command.getId(), command.getStripeSellerAccountId(),
        command.getStripeOnboardingLink()));
  }

  @EventSourcingHandler
  public void on(StripeSellerAccountCreatedEvent event) {
    this.stripeSellerAccountId = event.getStripeSellerAccountId();
  }

  @CommandHandler
  public void on(UpdateStripeSellerStatusCommand command) {
    if (this.sellerAccountEnabled == command.isEnabled()) {
      return; // No change
    }
    apply(new StripeSellerStatusUpdatedEvent(command.getId(), command.isEnabled()));
  }

  @EventSourcingHandler
  public void on(StripeSellerStatusUpdatedEvent event) {
    this.sellerAccountEnabled = event.isEnabled();
  }

  @CommandHandler
  public void on(RemoveUserCommand command) {
    apply(new UserRemovedEvent(command.getId(), this.keycloakUserId));
  }

  @EventSourcingHandler
  public void on(UserRemovedEvent event) {
    markDeleted();
  }
}
