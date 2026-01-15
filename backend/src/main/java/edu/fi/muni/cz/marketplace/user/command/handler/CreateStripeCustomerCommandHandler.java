package edu.fi.muni.cz.marketplace.user.command.handler;

import edu.fi.muni.cz.marketplace.user.command.CreateStripeCustomerCommand;
import edu.fi.muni.cz.marketplace.user.event.StripeCustomerRegisteredEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;

import edu.fi.muni.cz.marketplace.user.service.StripeApiClient;
import lombok.RequiredArgsConstructor;

/**
 * Handles the creation of a Stripe customer.
 * <p>
 * Calls {@link StripeApiClient} to create the customer in Stripe and then
 * publishes
 * a {@link StripeCustomerRegisteredEvent} to update the user aggregate.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CreateStripeCustomerCommandHandler {

  private final StripeApiClient stripeClient;

  private final EventGateway eventGateway;

  @CommandHandler
  public void on(CreateStripeCustomerCommand command) {
    String customerId = stripeClient.createCustomer(command.getId(), command.getEmail(), command.getName(),
        command.getPhone(), command.getShippingAddress());

    eventGateway.publish(new StripeCustomerRegisteredEvent(command.getId(), customerId));
  }
}
