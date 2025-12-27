package edu.fi.muni.cz.marketplace.user.command;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import edu.fi.muni.cz.marketplace.user.service.StripeApiClient;
import lombok.RequiredArgsConstructor;

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
