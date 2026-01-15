package edu.fi.muni.cz.marketplace.user.event.handler;

import edu.fi.muni.cz.marketplace.user.command.AssignStripeCustomerIdCommand;
import edu.fi.muni.cz.marketplace.user.command.AssignStripeSellerAccountIdCommand;
import edu.fi.muni.cz.marketplace.user.event.StripeConnectedAccountCreatedEvent;
import edu.fi.muni.cz.marketplace.user.event.StripeCustomerRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ProcessingGroup("stripe_integration")
@RequiredArgsConstructor
public class StripeIntegrationEventHandler {

    private final CommandGateway commandGateway;

    @EventHandler
    public void on(StripeConnectedAccountCreatedEvent event) {
        log.info("Handling StripeConnectedAccountCreatedEvent for user: {}", event.getUserId());
        commandGateway.send(new AssignStripeSellerAccountIdCommand(event.getUserId(), event.getStripeAccountId()));
    }

    @EventHandler
    public void on(StripeCustomerRegisteredEvent event) {
        log.info("Handling StripeCustomerRegisteredEvent for user: {}", event.getUserId());
        commandGateway.send(new AssignStripeCustomerIdCommand(event.getUserId(), event.getStripeCustomerId()));
    }
}
