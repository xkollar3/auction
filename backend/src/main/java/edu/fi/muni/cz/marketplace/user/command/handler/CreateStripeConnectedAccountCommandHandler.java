package edu.fi.muni.cz.marketplace.user.command.handler;

import edu.fi.muni.cz.marketplace.user.command.CreateStripeConnectedAccountCommand;
import edu.fi.muni.cz.marketplace.user.event.StripeConnectedAccountCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

import edu.fi.muni.cz.marketplace.user.service.StripeApiClient;
import edu.fi.muni.cz.marketplace.user.service.dto.ConnectedAccountResponse;

/**
 * Handles the creation of a Stripe Connect Express account.
 * <p>
 * Orchestrates the flow:
 * 1. Creates a Stripe Connected Account.
 * 2. Publishes {@link StripeConnectedAccountCreatedEvent} (handled
 * asynchronously to assign account ID).
 * 3. Creates and returns a Stripe Account Link for onboarding.
 * </p>
 */
@Service
public class CreateStripeConnectedAccountCommandHandler {

    private final StripeApiClient stripeClient;
    private final EventGateway eventGateway;

    private final String refreshUrl;
    private final String returnUrl;

    public CreateStripeConnectedAccountCommandHandler(
            StripeApiClient stripeClient,
            EventGateway eventGateway,
            @Value("${stripe.seller.refresh-url}") String refreshUrl,
            @Value("${stripe.seller.return-url}") String returnUrl) {
        this.stripeClient = stripeClient;
        this.eventGateway = eventGateway;
        this.refreshUrl = refreshUrl;
        this.returnUrl = returnUrl;
    }

    @CommandHandler
    public String on(CreateStripeConnectedAccountCommand command) {
        ConnectedAccountResponse accountResponse = stripeClient.createConnectedAccount(command.getId(),
                command.getEmail());

        eventGateway.publish(new StripeConnectedAccountCreatedEvent(command.getId(), accountResponse.accountId()));

        return stripeClient.createAccountLink(accountResponse.accountId(), refreshUrl, returnUrl);
    }
}
