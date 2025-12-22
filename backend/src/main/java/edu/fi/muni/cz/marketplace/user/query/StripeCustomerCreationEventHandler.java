package edu.fi.muni.cz.marketplace.user.query;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import edu.fi.muni.cz.marketplace.user.command.CompleteStripeCustomerCreationCommand;
import edu.fi.muni.cz.marketplace.user.command.FailStripeCustomerCreationCommand;
import edu.fi.muni.cz.marketplace.user.event.StripeCustomerCreationInitiatedEvent;
import edu.fi.muni.cz.marketplace.user.service.StripeClient;
import edu.fi.muni.cz.marketplace.user.service.StripeCustomerCreationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripeCustomerCreationEventHandler {

  private final StripeClient stripeClient;
  private final CommandGateway commandGateway;

  @EventHandler
  public void on(StripeCustomerCreationInitiatedEvent event) {
    log.info("Handling StripeCustomerCreationInitiatedEvent for aggregate: {}", event.getId());

    try {
      String stripeCustomerId = stripeClient.createCustomer(
          event.getId(),
          event.getEmail(),
          event.getName(),
          event.getPhone(),
          event.getShippingAddress()
      );

      log.info("Stripe customer created successfully: {}, dispatching completion command", stripeCustomerId);
      commandGateway.sendAndWait(new CompleteStripeCustomerCreationCommand(
          event.getId(),
          stripeCustomerId
      ));

    } catch (StripeCustomerCreationException e) {
      log.error("Stripe customer creation failed for aggregate {}: {}", event.getId(), e.getMessage());
      commandGateway.sendAndWait(new FailStripeCustomerCreationCommand(
          event.getId(),
          e.getMessage()
      ));
    }
  }
}
