package edu.fi.muni.cz.marketplace.user.command.handler;

import edu.fi.muni.cz.marketplace.user.command.AssignStripeCustomerIdCommand;
import edu.fi.muni.cz.marketplace.user.command.CreateStripeCustomerCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import edu.fi.muni.cz.marketplace.user.service.StripeApiClient;
import lombok.RequiredArgsConstructor;

/**
 * Handles the creation of a Stripe customer.
 * <p>
 * Calls {@link StripeApiClient} to create the customer in Stripe and then
 * dispatches
 * an {@link AssignStripeCustomerIdCommand} to update the user aggregate.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CreateStripeCustomerCommandHandler {

  private final StripeApiClient stripeClient;

  private final CommandGateway commandGateway;

  @CommandHandler
  public void on(CreateStripeCustomerCommand command) {
    String customerId = stripeClient.createCustomer(command.getId(), command.getEmail(), command.getName(),
        command.getPhone(), command.getShippingAddress());

    commandGateway.send(new AssignStripeCustomerIdCommand(command.getId(), customerId));
  }
}
